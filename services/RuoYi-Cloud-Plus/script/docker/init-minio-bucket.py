#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""初始化 MinIO 桶（本仓库 docker-compose 的 minio 服务）。

为什么需要这个脚本：
    本仓库的 `ruoyi-common-oss` 走的是 AWS S3 异步 SDK（`S3AsyncClient`），
    不像上游的 MinIO SDK 那样在建客户端时 `createBucket`；
    而 docker-compose 起 MinIO 时也不会建桶。桶不存在时，上传接口会返回
    `OssException: 上传文件失败，请检查配置信息:[subscription has been cancelled.]`
    （真实原因 `NoSuchBucket` 被 SDK 吞掉了），并且 `sys_oss_config.access_policy=1`（公开）
    并不会自动给桶打策略，导致上传成功但前端拿到 URL 后 403。

用法（在仓库根目录执行，默认参数与 script/docker/docker-compose.yml 一致）：
    python services/RuoYi-Cloud-Plus/script/docker/init-minio-bucket.py
    python .../init-minio-bucket.py --host 127.0.0.1 --port 9000 --access-key ruoyi --secret-key ruoyi123 --bucket ruoyi

只依赖标准库；幂等：桶已存在则跳过创建，策略每次覆盖为「公开只读」。
"""

from __future__ import annotations

import argparse
import datetime
import hashlib
import hmac
import json
import sys
import urllib.error
import urllib.parse
import urllib.request

REGION = "us-east-1"
SERVICE = "s3"


class SigV4:
    def __init__(self, host: str, access_key: str, secret_key: str) -> None:
        self.host = host
        self.access_key = access_key
        self.secret_key = secret_key

    def headers(self, method: str, path: str, payload: bytes = b"") -> dict:
        now = datetime.datetime.now(datetime.timezone.utc)
        amzdate = now.strftime("%Y%m%dT%H%M%SZ")
        datestamp = now.strftime("%Y%m%d")
        payload_hash = hashlib.sha256(payload).hexdigest()
        canonical_headers = (
            f"host:{self.host}\nx-amz-content-sha256:{payload_hash}\nx-amz-date:{amzdate}\n"
        )
        signed_headers = "host;x-amz-content-sha256;x-amz-date"
        # SigV4 要求把 query string 从路径里拆出来单独规范化（如 ?policy -> policy=），否则签名不匹配
        split = urllib.parse.urlsplit(path)
        canonical_uri = urllib.parse.quote(split.path or "/", safe="/-_.~")
        pairs = urllib.parse.parse_qsl(split.query, keep_blank_values=True)
        canonical_query = "&".join(
            f"{urllib.parse.quote(k, safe='-_.~')}={urllib.parse.quote(v, safe='-_.~')}"
            for k, v in sorted(pairs)
        )
        canonical_request = "\n".join(
            [method, canonical_uri, canonical_query, canonical_headers, signed_headers, payload_hash]
        )
        scope = f"{datestamp}/{REGION}/{SERVICE}/aws4_request"
        string_to_sign = "\n".join(
            ["AWS4-HMAC-SHA256", amzdate, scope, hashlib.sha256(canonical_request.encode()).hexdigest()]
        )

        def _h(key: bytes, msg: str) -> bytes:
            return hmac.new(key, msg.encode(), hashlib.sha256).digest()

        k = _h(("AWS4" + self.secret_key).encode(), datestamp)
        k = _h(k, REGION)
        k = _h(k, SERVICE)
        k = _h(k, "aws4_request")
        signature = hmac.new(k, string_to_sign.encode(), hashlib.sha256).hexdigest()
        return {
            "x-amz-date": amzdate,
            "x-amz-content-sha256": payload_hash,
            "Authorization": (
                f"AWS4-HMAC-SHA256 Credential={self.access_key}/{scope}, "
                f"SignedHeaders={signed_headers}, Signature={signature}"
            ),
        }


def call(signer: SigV4, base: str, method: str, path: str, payload: bytes = b"", content_type: str | None = None):
    headers = signer.headers(method, path, payload)
    if content_type:
        headers["Content-Type"] = content_type
    request = urllib.request.Request(
        base + path, data=payload if method in ("PUT", "POST") else None, method=method, headers=headers
    )
    try:
        with urllib.request.urlopen(request, timeout=20) as response:
            return response.status, response.read()
    except urllib.error.HTTPError as error:
        return error.code, error.read()


def main() -> int:
    parser = argparse.ArgumentParser(description="初始化 MinIO 桶并设置为公开只读")
    parser.add_argument("--host", default="127.0.0.1")
    parser.add_argument("--port", type=int, default=9000)
    parser.add_argument("--access-key", default="ruoyi")
    parser.add_argument("--secret-key", default="ruoyi123")
    parser.add_argument("--bucket", default="ruoyi")
    parser.add_argument("--private", action="store_true", help="只建桶，不设置公开只读策略")
    args = parser.parse_args()

    host = f"{args.host}:{args.port}"
    base = f"http://{host}"
    signer = SigV4(host, args.access_key, args.secret_key)

    status, _ = call(signer, base, "HEAD", f"/{args.bucket}")
    if status == 200:
        print(f"[skip] bucket 已存在：{args.bucket}")
    else:
        status, body = call(signer, base, "PUT", f"/{args.bucket}")
        if status not in (200, 409):
            print(f"[fail] 创建 bucket 失败：{status} {body[:200]!r}", file=sys.stderr)
            return 1
        print(f"[ok] 已创建 bucket：{args.bucket}")

    if not args.private:
        policy = {
            "Version": "2012-10-17",
            "Statement": [
                {
                    "Effect": "Allow",
                    "Principal": {"AWS": ["*"]},
                    "Action": ["s3:GetObject"],
                    "Resource": [f"arn:aws:s3:::{args.bucket}/*"],
                }
            ],
        }
        payload = json.dumps(policy).encode()
        status, body = call(signer, base, "PUT", f"/{args.bucket}?policy", payload, "application/json")
        if status not in (200, 204):
            print(f"[fail] 设置公开只读策略失败：{status} {body[:200]!r}", file=sys.stderr)
            return 1
        print("[ok] 已设置公开只读策略（匿名可 GetObject）")

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
