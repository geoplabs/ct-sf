# API Versioning Guidelines

This document outlines the API versioning approach used in the Carbon Tracker application.

## Versioning Strategy

We use URL path versioning to clearly indicate the version of the API being used. All API endpoints are prefixed with `/api/v{n}/` where `{n}` is the version number.

Current API version: **v1**

## API Version Constants

API version paths are defined as constants in `ApiVersionConfig.kt` to ensure consistency throughout the application:

```kotlin
object ApiVersionConfig {
    // Base API path with version
    const val API_V1_BASE = "/api/v1"

    // Resource endpoints
    const val API_V1_USERS = "$API_V1_BASE/users"
    const val API_V1_GROUPS = "$API_V1_BASE/groups"
    const val API_V1_AUTH = "$API_V1_BASE/access-management/auth"
}
```

## Using the API Version Constants

When creating or modifying API endpoints, always use the constants from `ApiVersionConfig` instead of hardcoding paths:

```kotlin
@RestController
@RequestMapping(ApiVersionConfig.API_V1_USERS)
class UserController(private val userService: UserService) {
    // ...
}
```

## Future Versioning

When introducing new versions:

1. Create a new version constant in `ApiVersionConfig`
2. Implement new controller classes for the new version
3. Document changes between versions for client developers

## API Endpoints

Current endpoints:

- **Users API**: `/api/v1/users`
- **Groups API**: `/api/v1/groups`
- **Auth API**: `/api/v1/access-management/auth`

## Client Impact

Clients should be instructed to use the versioned endpoints to ensure their applications continue working when API changes are made in future versions. 