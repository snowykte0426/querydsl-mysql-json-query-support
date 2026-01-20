# Security Policy

## Supported Versions

We only provide security updates for official release versions 0.1.0 and above.

| Version  | Supported |
|----------|-----------|
| >= 0.1.0 | ✅         |
| < 0.1.0  | ❌         |

### Official Release Definition
- **Official Releases:** Versions without any flags (e.g., `0.1.0`, `1.0.0`). These versions are fully supported.
- **Non-Official Versions:** Versions with flags such as `-Beta.x`, `-Dev.x`, or `-rc.x` (e.g., `0.1.0-Beta.1`) are **not** covered by this security policy. We recommend upgrading to an official release for production environments.

## Reporting a Vulnerability

If you discover a security vulnerability within this project, please do not use the public issue tracker. Instead, please report it via the following method:

1. Send an email to **snowykte0426@naver.com**.
2. Include a detailed description of the vulnerability.
3. Provide steps to reproduce the issue (a proof-of-concept is highly appreciated).

We will acknowledge your report within 48 hours and provide a timeline for a fix if the vulnerability is confirmed.

## Security Best Practices

While we strive to keep this library secure, we recommend following these practices when using JSON functions:
- Always use the type-safe methods provided by this library to prevent SQL injection.
- Be cautious when handling raw JSON strings from untrusted sources.
- Use the convenience methods (e.g., `jsonContainsString`) which handle automatic escaping when possible.
