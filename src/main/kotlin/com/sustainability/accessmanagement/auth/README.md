# Access Management Authentication Module

This module provides authentication and token management functionality that can be used across different parts of the application. It is designed to be decoupled from any specific module to promote reusability.

## Components

1. **TokenGenerator** - Utility for generating JWT tokens with various claims
2. **JwtAuthFilter** - Servlet filter that validates JWT tokens and creates security contexts
3. **TokenController** - REST endpoints for generating test tokens
4. **GenerateJwtToken** - Command-line tool for token generation

## Integration with Other Modules

Any module in the application can use this authentication system by:

1. Including the JwtAuthFilter in their request processing chain
2. Accessing the SecurityContext from request attributes
3. Using module-specific adapters to convert the generic SecurityContext to their own format

### Example: Impacts Module Integration

The Impacts module uses the authentication system through:

```kotlin
// Adapter to convert AccessManagement SecurityContext to Impacts SecurityContext
class SecurityContextAdapter {
    fun getImpactsSecurityContext(request: HttpServletRequest): SecurityContext? {
        val accessManagementContext = request.getAttribute("securityContext") as? AccessManagementSecurityContext
            ?: return null
            
        // Convert between context types...
        return SecurityContext(
            userId = userId,
            groupId = accessManagementContext.groupId,
            groupRoles = groupRoles
        )
    }
}
```

## Token Generation for Testing

### Method 1: REST API

```http
POST /access-management/auth/token
Content-Type: application/json

{
  "email": "john.doe@example.com",
  "groupId": "group-sustainability-123",
  "role": "contributor"
}
```

### Method 2: Command Line Tool

```bash
kotlin com.sustainability.accessmanagement.auth.tools.GenerateJwtToken john.doe@example.com group-123 contributor
```

### Method 3: Direct API Usage

```kotlin
import com.sustainability.accessmanagement.auth.TokenGenerator

val token = TokenGenerator.createTestToken(
    email = "john.doe@example.com",
    groupId = "group-123",
    role = "contributor"
)
```

## Token Usage

Include the generated token in API requests using these headers:

```
Authorization: Bearer {jwt_token}
X-Group-Id: {group_id}
```

## Security Context Structure

The SecurityContext created by the authentication system contains:

```kotlin
data class SecurityContext(
    val email: String,
    val groupId: String,
    val groupRoles: Map<String, UserGroupRole>,
    val scope: SecurityScope? = null,
)
```

This context is available to the application as a request attribute named "securityContext". 