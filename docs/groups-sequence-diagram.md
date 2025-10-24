# Groups Management Sequence Diagrams

This document provides comprehensive sequence diagrams for the Groups Management System, covering different use cases including parent groups, child groups (subgroups), user creation, and roles application.

## Table of Contents

1. [System Overview](#system-overview)
2. [Group Hierarchy Creation](#group-hierarchy-creation)
3. [User Management and Role Assignment](#user-management-and-role-assignment)
4. [Group Access Control](#group-access-control)
5. [Complex Scenarios](#complex-scenarios)

## System Overview

The system supports hierarchical group structures with the following key components:
- **Groups**: Hierarchical structure with parent-child relationships
- **Users**: Can belong to multiple groups with different roles
- **Roles**: ADMIN, CONTRIBUTOR, READER (hierarchical permissions)
- **Security Context**: Authorization based on group membership and roles

### Role Hierarchy
- **ADMIN**: Full access (create, read, update, delete)
- **CONTRIBUTOR**: Read and write access
- **READER**: Read-only access

## Group Hierarchy Creation

### Use Case 1: Creating Parent Company and Subsidiaries

```plantuml
@startuml
title Group Hierarchy Creation - Parent Company and Subsidiaries

actor "System Admin" as Admin
participant "API Gateway" as API
participant "AuthZ Service" as AuthZ
participant "Group Service" as GroupSvc
participant "Group Repository" as GroupRepo
participant "Database" as DB

== Create Parent Company ==
Admin -> API: POST /api/v1/groups\n{name: "Reliance", isParentCompany: true}
API -> AuthZ: Validate JWT Token
AuthZ -> AuthZ: Extract user context\n(email, groupId: "/", roles: {"/": "ADMIN"})
API -> GroupSvc: create(securityContext, newGroupRequest)

GroupSvc -> GroupSvc: Authorization Check\n- Must have ADMIN role in current group
GroupSvc -> GroupSvc: Generate Group ID: "/Reliance"
GroupSvc -> GroupSvc: Generate Account ID (for parent company)
GroupSvc -> GroupRepo: create(group)
GroupRepo -> DB: Insert group record
DB --> GroupRepo: Success
GroupRepo --> GroupSvc: Group created
GroupSvc --> API: Return created group
API --> Admin: 201 Created\n{id: "/Reliance", accountId: "ACC-123"}

== Create Subsidiary (Child Group) ==
Admin -> API: POST /api/v1/groups\n{name: "Jio", description: "Telecom subsidiary"}\nX-Group-Context: /Reliance
API -> AuthZ: Validate JWT Token
AuthZ -> AuthZ: Extract user context\n(email, groupId: "/Reliance", roles: {"/Reliance": "ADMIN"})
API -> GroupSvc: create(securityContext, newGroupRequest)

GroupSvc -> GroupSvc: Authorization Check\n- Must have ADMIN role in /Reliance
GroupSvc -> GroupSvc: Generate Group ID: "/Reliance/Jio"
GroupSvc -> GroupSvc: Check parent-child relationship
GroupSvc -> GroupRepo: create(group)
GroupRepo -> DB: Insert group record
DB --> GroupRepo: Success
GroupRepo --> GroupSvc: Group created
GroupSvc --> API: Return created group
API --> Admin: 201 Created\n{id: "/Reliance/Jio"}

== Create Sub-subsidiary (Grandchild Group) ==
Admin -> API: POST /api/v1/groups\n{name: "JioFiber", description: "Fiber services"}\nX-Group-Context: /Reliance/Jio
API -> AuthZ: Validate JWT Token
AuthZ -> AuthZ: Extract user context\n(email, groupId: "/Reliance/Jio", roles: {"/Reliance": "ADMIN"})
API -> GroupSvc: create(securityContext, newGroupRequest)

GroupSvc -> GroupSvc: Authorization Check\n- Must have ADMIN role in /Reliance/Jio
GroupSvc -> GroupSvc: Generate Group ID: "/Reliance/Jio/JioFiber"
GroupSvc -> GroupRepo: create(group)
GroupRepo -> DB: Insert group record
DB --> GroupRepo: Success
GroupRepo --> GroupSvc: Group created
GroupSvc --> API: Return created group
API --> Admin: 201 Created\n{id: "/Reliance/Jio/JioFiber"}

@enduml
```

## User Management and Role Assignment

### Use Case 2: User Creation and Role Assignment Across Groups

```plantuml
@startuml
title User Management and Role Assignment

actor "Group Admin" as Admin
actor "New User" as NewUser
participant "API Gateway" as API
participant "AuthZ Service" as AuthZ
participant "User Service" as UserSvc
participant "Group Service" as GroupSvc
participant "User Repository" as UserRepo
participant "Cognito" as Cognito
participant "Database" as DB

== Create User in Parent Group ==
Admin -> API: POST /api/v1/users\n{email: "john@reliance.com", role: "CONTRIBUTOR"}\nX-Group-Context: /Reliance
API -> AuthZ: Validate JWT Token
AuthZ -> AuthZ: Extract admin context\n(email: "admin@reliance.com", groupId: "/Reliance", roles: {"/Reliance": "ADMIN"})
API -> UserSvc: grant(securityContext, newUserRequest)

UserSvc -> UserSvc: Authorization Check\n- Must have ADMIN role in /Reliance
UserSvc -> UserSvc: Check if user exists
UserSvc -> UserSvc: Create user with role mapping\n{"/Reliance": "CONTRIBUTOR"}
UserSvc -> UserRepo: create(user)
UserRepo -> DB: Insert user record
DB --> UserRepo: Success
UserRepo --> UserSvc: User created
UserSvc --> API: Return created user
API --> Admin: 201 Created\n{email: "john@reliance.com", groups: {"/Reliance": "CONTRIBUTOR"}}

== Grant Access to Subsidiary ==
Admin -> API: POST /api/v1/users\n{email: "john@reliance.com", role: "ADMIN"}\nX-Group-Context: /Reliance/Jio
API -> AuthZ: Validate JWT Token
AuthZ -> AuthZ: Extract admin context\n(email: "admin@reliance.com", groupId: "/Reliance/Jio", roles: {"/Reliance": "ADMIN"})
API -> UserSvc: grant(securityContext, newUserRequest)

UserSvc -> UserSvc: Authorization Check\n- Must have ADMIN role in /Reliance/Jio
UserSvc -> UserRepo: get("john@reliance.com")
UserRepo -> DB: Query user
DB --> UserRepo: Existing user found
UserRepo --> UserSvc: User exists
UserSvc -> UserSvc: Add group to existing user\n{"/Reliance": "CONTRIBUTOR", "/Reliance/Jio": "ADMIN"}
UserSvc -> UserRepo: update(user)
UserRepo -> DB: Update user record
DB --> UserRepo: Success
UserRepo --> UserSvc: User updated
UserSvc --> API: Return updated user
API --> Admin: 200 OK\n{email: "john@reliance.com", groups: {"/Reliance": "CONTRIBUTOR", "/Reliance/Jio": "ADMIN"}}

== User Authentication and Context Setting ==
NewUser -> API: Login Request
API -> Cognito: Authenticate user
Cognito --> API: JWT Token with groups\n["Reliance|||contributor", "Reliance/Jio|||admin"]
API --> NewUser: JWT Token

NewUser -> API: GET /api/v1/groups\nAuthorization: Bearer <token>\nX-Group-Context: /Reliance/Jio
API -> AuthZ: Validate JWT Token
AuthZ -> AuthZ: Parse groups from token\n{"/Reliance": "CONTRIBUTOR", "/Reliance/Jio": "ADMIN"}
AuthZ -> AuthZ: Validate group context\n- /Reliance/Jio is child of user's groups
AuthZ -> AuthZ: Set security context\n(email: "john@reliance.com", groupId: "/Reliance/Jio", roles: {...})
API -> GroupSvc: list(securityContext)
GroupSvc --> API: Groups accessible to user
API --> NewUser: 200 OK with groups

@enduml
```

### Use Case 3: Role-Based Access Control in Action

```plantuml
@startuml
title Role-Based Access Control Scenarios

actor "ADMIN User" as Admin
actor "CONTRIBUTOR User" as Contributor
actor "READER User" as Reader
participant "API Gateway" as API
participant "AuthZ Service" as AuthZ
participant "Group Service" as GroupSvc
participant "User Service" as UserSvc

== ADMIN Operations ==
Admin -> API: POST /api/v1/groups\n{name: "NewSubsidiary"}\nX-Group-Context: /Reliance
API -> AuthZ: Validate (groupId: "/Reliance", role: "ADMIN")
AuthZ --> API: Authorized
API -> GroupSvc: create(securityContext, newGroup)
GroupSvc -> GroupSvc: Check ADMIN role required ✓
GroupSvc --> API: Group created
API --> Admin: 201 Created

Admin -> API: POST /api/v1/users\n{email: "new@user.com", role: "READER"}
API -> AuthZ: Validate (groupId: "/Reliance", role: "ADMIN")
AuthZ --> API: Authorized
API -> UserSvc: grant(securityContext, newUser)
UserSvc -> UserSvc: Check ADMIN role required ✓
UserSvc --> API: User granted access
API --> Admin: 201 Created

== CONTRIBUTOR Operations ==
Contributor -> API: PATCH /api/v1/groups/Reliance\n{description: "Updated description"}
API -> AuthZ: Validate (groupId: "/Reliance", role: "CONTRIBUTOR")
AuthZ --> API: Authorized
API -> GroupSvc: update(securityContext, groupId, updates)
GroupSvc -> GroupSvc: Check ADMIN role required ✗
GroupSvc --> API: UnauthorizedException
API --> Contributor: 403 Forbidden

Contributor -> API: GET /api/v1/groups/Reliance
API -> AuthZ: Validate (groupId: "/Reliance", role: "CONTRIBUTOR")
AuthZ --> API: Authorized
API -> GroupSvc: get(securityContext, groupId)
GroupSvc -> GroupSvc: Check READER role required ✓
GroupSvc --> API: Group details
API --> Contributor: 200 OK

== READER Operations ==
Reader -> API: GET /api/v1/groups
API -> AuthZ: Validate (groupId: "/Reliance", role: "READER")
AuthZ --> API: Authorized
API -> GroupSvc: list(securityContext)
GroupSvc -> GroupSvc: Check READER role required ✓
GroupSvc --> API: Groups list
API --> Reader: 200 OK

Reader -> API: POST /api/v1/groups\n{name: "Unauthorized"}
API -> AuthZ: Validate (groupId: "/Reliance", role: "READER")
AuthZ --> API: Authorized
API -> GroupSvc: create(securityContext, newGroup)
GroupSvc -> GroupSvc: Check ADMIN role required ✗
GroupSvc --> API: UnauthorizedException
API --> Reader: 403 Forbidden

@enduml
```

## Group Access Control

### Use Case 4: Hierarchical Access Control

```plantuml
@startuml
title Hierarchical Access Control

actor "Parent Admin" as ParentAdmin
actor "Child Admin" as ChildAdmin
participant "API Gateway" as API
participant "AuthZ Service" as AuthZ
participant "Group Service" as GroupSvc
participant "Group Permissions" as GroupPerms

== Parent Admin Accessing Child Group ==
ParentAdmin -> API: GET /api/v1/groups/Reliance/Jio\nX-Group-Context: /Reliance
API -> AuthZ: Validate JWT Token
AuthZ -> AuthZ: Extract context\n(groupId: "/Reliance", roles: {"/Reliance": "ADMIN"})
API -> GroupSvc: get(securityContext, "/Reliance/Jio")

GroupSvc -> GroupSvc: Authorization Check
GroupSvc -> GroupSvc: isParentOf("/Reliance", "/Reliance/Jio") = true
GroupSvc -> GroupPerms: isAuthorized(["/Reliance"], {"/Reliance": "ADMIN"}, ["READER"], "ALL")
GroupPerms -> GroupPerms: Check hierarchical permissions
GroupPerms --> GroupSvc: Authorized ✓
GroupSvc --> API: Group details
API --> ParentAdmin: 200 OK

== Child Admin Accessing Parent Group ==
ChildAdmin -> API: GET /api/v1/groups/Reliance\nX-Group-Context: /Reliance/Jio
API -> AuthZ: Validate JWT Token
AuthZ -> AuthZ: Extract context\n(groupId: "/Reliance/Jio", roles: {"/Reliance/Jio": "ADMIN"})
API -> GroupSvc: get(securityContext, "/Reliance")

GroupSvc -> GroupSvc: Authorization Check
GroupSvc -> GroupSvc: isParentOf("/Reliance/Jio", "/Reliance") = false
GroupSvc -> GroupSvc: isParentOf("/Reliance", "/Reliance/Jio") = true
GroupSvc -> GroupPerms: isAuthorized(["/Reliance/Jio"], {"/Reliance/Jio": "ADMIN"}, ["READER"], "ALL")
GroupPerms -> GroupPerms: Check permissions - no access to parent
GroupPerms --> GroupSvc: Unauthorized ✗
GroupSvc --> API: UnauthorizedException
API --> ChildAdmin: 403 Forbidden

== Cross-Subsidiary Access ==
ChildAdmin -> API: GET /api/v1/groups/Reliance/Retail\nX-Group-Context: /Reliance/Jio
API -> AuthZ: Validate JWT Token
AuthZ -> AuthZ: Extract context\n(groupId: "/Reliance/Jio", roles: {"/Reliance/Jio": "ADMIN"})
API -> GroupSvc: get(securityContext, "/Reliance/Retail")

GroupSvc -> GroupSvc: Authorization Check
GroupSvc -> GroupSvc: isParentOf("/Reliance/Jio", "/Reliance/Retail") = false
GroupSvc -> GroupSvc: isParentOf("/Reliance/Retail", "/Reliance/Jio") = false
GroupSvc -> GroupPerms: isAuthorized(["/Reliance/Jio"], {"/Reliance/Jio": "ADMIN"}, ["READER"], "ALL")
GroupPerms -> GroupPerms: No hierarchical relationship
GroupPerms --> GroupSvc: Unauthorized ✗
GroupSvc --> API: UnauthorizedException
API --> ChildAdmin: 403 Forbidden

@enduml
```

## Complex Scenarios

### Use Case 5: Multi-Group User with Different Roles

```plantuml
@startuml
title Multi-Group User with Different Roles

actor "Multi-Group User" as User
participant "API Gateway" as API
participant "AuthZ Service" as AuthZ
participant "Group Service" as GroupSvc
participant "User Service" as UserSvc

== User with Multiple Group Memberships ==
note over User: User has roles:\n- /Reliance: READER\n- /Reliance/Jio: ADMIN\n- /Reliance/Retail: CONTRIBUTOR

== Context: Operating as Jio Admin ==
User -> API: POST /api/v1/users\n{email: "new@jio.com", role: "CONTRIBUTOR"}\nX-Group-Context: /Reliance/Jio
API -> AuthZ: Validate JWT Token
AuthZ -> AuthZ: Extract context\n(groupId: "/Reliance/Jio", roles: {"/Reliance": "READER", "/Reliance/Jio": "ADMIN", "/Reliance/Retail": "CONTRIBUTOR"})
API -> UserSvc: grant(securityContext, newUser)

UserSvc -> UserSvc: Authorization Check for /Reliance/Jio
UserSvc -> UserSvc: Check ADMIN role in /Reliance/Jio ✓
UserSvc --> API: User created successfully
API --> User: 201 Created

== Context: Operating as Retail Contributor ==
User -> API: POST /api/v1/users\n{email: "new@retail.com", role: "READER"}\nX-Group-Context: /Reliance/Retail
API -> AuthZ: Validate JWT Token
AuthZ -> AuthZ: Extract context\n(groupId: "/Reliance/Retail", roles: {"/Reliance": "READER", "/Reliance/Jio": "ADMIN", "/Reliance/Retail": "CONTRIBUTOR"})
API -> UserSvc: grant(securityContext, newUser)

UserSvc -> UserSvc: Authorization Check for /Reliance/Retail
UserSvc -> UserSvc: Check ADMIN role in /Reliance/Retail ✗ (only CONTRIBUTOR)
UserSvc --> API: UnauthorizedException
API --> User: 403 Forbidden

== Context: Trying to Access Parent Group ==
User -> API: GET /api/v1/groups/Reliance\nX-Group-Context: /Reliance
API -> AuthZ: Validate JWT Token
AuthZ -> AuthZ: Extract context\n(groupId: "/Reliance", roles: {"/Reliance": "READER", "/Reliance/Jio": "ADMIN", "/Reliance/Retail": "CONTRIBUTOR"})
API -> GroupSvc: get(securityContext, "/Reliance")

GroupSvc -> GroupSvc: Authorization Check
GroupSvc -> GroupSvc: Check READER role in /Reliance ✓
GroupSvc --> API: Group details (limited by READER role)
API --> User: 200 OK

@enduml
```

### Use Case 6: Group Deletion with Dependency Checks

```plantuml
@startuml
title Group Deletion with Dependency Checks

actor "Admin" as Admin
participant "API Gateway" as API
participant "AuthZ Service" as AuthZ
participant "Group Service" as GroupSvc
participant "User Service" as UserSvc
participant "Group Repository" as GroupRepo

== Attempt to Delete Group with Children ==
Admin -> API: DELETE /api/v1/groups/Reliance\nX-Group-Context: /
API -> AuthZ: Validate JWT Token
AuthZ -> AuthZ: Extract context\n(groupId: "/", roles: {"/": "ADMIN"})
API -> GroupSvc: delete(securityContext, "/Reliance")

GroupSvc -> GroupSvc: Authorization Check\n- Must be ADMIN of parent or self
GroupSvc -> GroupRepo: list(parentGroupId = "/Reliance")
GroupRepo --> GroupSvc: Child groups found: ["/Reliance/Jio", "/Reliance/Retail"]
GroupSvc --> API: InvalidRequestException\n"Cannot delete group with children"
API --> Admin: 400 Bad Request

== Delete Child Group First ==
Admin -> API: DELETE /api/v1/groups/Reliance/Jio\nX-Group-Context: /Reliance
API -> AuthZ: Validate JWT Token
AuthZ -> AuthZ: Extract context\n(groupId: "/Reliance", roles: {"/Reliance": "ADMIN"})
API -> GroupSvc: delete(securityContext, "/Reliance/Jio")

GroupSvc -> GroupSvc: Authorization Check ✓
GroupSvc -> GroupRepo: list(parentGroupId = "/Reliance/Jio")
GroupRepo --> GroupSvc: No child groups found
GroupSvc -> UserSvc: Check users in group
UserSvc --> GroupSvc: Users found - need to revoke access first
GroupSvc -> GroupSvc: Revoke all user access to group
GroupSvc -> GroupRepo: delete("/Reliance/Jio")
GroupRepo --> GroupSvc: Group deleted
GroupSvc --> API: Success
API --> Admin: 204 No Content

== Now Delete Parent Group ==
Admin -> API: DELETE /api/v1/groups/Reliance\nX-Group-Context: /
API -> AuthZ: Validate JWT Token
API -> GroupSvc: delete(securityContext, "/Reliance")

GroupSvc -> GroupSvc: Authorization Check ✓
GroupSvc -> GroupRepo: list(parentGroupId = "/Reliance")
GroupRepo --> GroupSvc: Remaining child: ["/Reliance/Retail"]
GroupSvc --> API: InvalidRequestException\n"Still has child groups"
API --> Admin: 400 Bad Request

@enduml
```

## Key Security Principles

1. **Hierarchical Authorization**: Parent group admins can access child groups, but not vice versa
2. **Role-Based Access Control**: Operations require specific minimum roles
3. **Context Validation**: Group context must be a child of user's assigned groups
4. **Dependency Management**: Groups with children or users cannot be deleted without cleanup
5. **Audit Trail**: All operations are logged with user context and timestamps

## Role Requirements Summary

| Operation | Required Role | Scope |
|-----------|---------------|-------|
| Create Group | ADMIN | Current group context |
| Update Group | ADMIN | Target group or its parent |
| Delete Group | ADMIN | Target group or its parent |
| List Groups | READER | Current group context |
| Get Group | READER | Target group or hierarchically related |
| Grant User Access | ADMIN | Current group context |
| Revoke User Access | ADMIN | Current group context |
| Update User | ADMIN (for state changes) | All user's groups |
| List Users | READER | Current group context | 