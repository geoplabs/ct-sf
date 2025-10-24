# Groups Management System - Architecture Overview

This document provides a high-level overview of the Groups Management System architecture and component relationships.

## System Architecture Diagram

```plantuml
@startuml
title Groups Management System - Architecture Overview

!define RECTANGLE class

package "Client Layer" {
    [Web Application] as WebApp
    [Mobile App] as MobileApp
    [API Client] as APIClient
}

package "API Gateway Layer" {
    [API Gateway] as Gateway
    [Load Balancer] as LB
}

package "Authentication & Authorization" {
    [JWT Token Service] as JWT
    [AuthZ Service] as AuthZ
    [Cognito] as Cognito
    [Group Permissions] as GroupPerms
}

package "Business Logic Layer" {
    [Group Service] as GroupSvc
    [User Service] as UserSvc
    [Security Utils] as SecurityUtils
}

package "Data Access Layer" {
    [Group Repository] as GroupRepo
    [User Repository] as UserRepo
}

package "Data Storage" {
    database "MongoDB" as DB {
        [Groups Collection] as GroupsCol
        [Users Collection] as UsersCol
    }
}

package "External Services" {
    [Email Service] as Email
    [Audit Service] as Audit
}

' Client connections
WebApp --> LB
MobileApp --> LB
APIClient --> LB

' Gateway layer
LB --> Gateway

' Authentication flow
Gateway --> JWT : Validate Token
Gateway --> AuthZ : Authorize Request
AuthZ --> Cognito : User Groups
AuthZ --> GroupPerms : Check Permissions

' Business logic
Gateway --> GroupSvc : Group Operations
Gateway --> UserSvc : User Operations
GroupSvc --> SecurityUtils : Authorization Checks
UserSvc --> SecurityUtils : Authorization Checks

' Data access
GroupSvc --> GroupRepo
UserSvc --> UserRepo
GroupRepo --> GroupsCol
UserRepo --> UsersCol

' External services
UserSvc --> Email : User Invitations
GroupSvc --> Audit : Operation Logs
UserSvc --> Audit : Operation Logs

@enduml
```

## Group Hierarchy Structure

```plantuml
@startuml
title Group Hierarchy Structure

package "Root Group (/)" {
    note top : System root\nSuper Admin access

    package "Parent Company (/Reliance)" {
        note top : Parent Company\nAccount ID: ACC-123
        
        package "Subsidiary 1 (/Reliance/Jio)" {
            note top : Telecom Division
            
            package "Sub-division (/Reliance/Jio/JioFiber)" {
                note top : Fiber Services
            }
            
            package "Sub-division (/Reliance/Jio/JioMobile)" {
                note top : Mobile Services
            }
        }
        
        package "Subsidiary 2 (/Reliance/Retail)" {
            note top : Retail Division
            
            package "Sub-division (/Reliance/Retail/Fashion)" {
                note top : Fashion Retail
            }
            
            package "Sub-division (/Reliance/Retail/Grocery)" {
                note top : Grocery Retail
            }
        }
        
        package "Subsidiary 3 (/Reliance/Energy)" {
            note top : Energy Division
        }
    }
    
    package "Another Company (/TCS)" {
        note top : IT Services Company
        
        package "Division (/TCS/Consulting)" {
            note top : Consulting Services
        }
        
        package "Division (/TCS/Development)" {
            note top : Software Development
        }
    }
}

@enduml
```

## User-Group-Role Relationships

```plantuml
@startuml
title User-Group-Role Relationships

entity "User" {
    * email : String
    --
    firstName : String
    lastName : String
    state : UserState
    defaultGroup : String
    groups : Map<String, UserGroupRole>
    tags : Map<String, Any>
    createdAt : Instant
    updatedAt : Instant
}

entity "Group" {
    * id : String
    --
    name : String
    description : String
    state : GroupState
    isParentCompany : Boolean
    accountId : String
    tags : Map<String, Any>
    configuration : Map<String, Any>
    createdAt : Instant
    updatedAt : Instant
}

enum "UserGroupRole" {
    ADMIN
    CONTRIBUTOR
    READER
}

enum "UserState" {
    INVITED
    ACTIVE
    DISABLED
}

enum "GroupState" {
    ACTIVE
    DISABLED
}

User ||--o{ UserGroupRole : "has roles in groups"
Group ||--o{ UserGroupRole : "contains users with roles"
User }o--|| UserState : "has state"
Group }o--|| GroupState : "has state"

note right of User : A user can belong to\nmultiple groups with\ndifferent roles in each

note left of Group : Groups form a hierarchy\nwith parent-child\nrelationships

@enduml
```

## Security Context Flow

```plantuml
@startuml
title Security Context Flow

participant "Client" as Client
participant "API Gateway" as Gateway
participant "AuthZ Service" as AuthZ
participant "JWT Service" as JWT
participant "Group Permissions" as GroupPerms
participant "Business Service" as Service

Client -> Gateway: Request with JWT Token\nX-Group-Context: /Reliance/Jio
Gateway -> JWT: Validate Token
JWT --> Gateway: Token Claims\n{email, groups: ["Reliance|||admin", "Reliance/Jio|||admin"]}

Gateway -> AuthZ: Extract Security Context
AuthZ -> AuthZ: Parse Groups from Token\n{"/Reliance": "ADMIN", "/Reliance/Jio": "ADMIN"}
AuthZ -> AuthZ: Validate Group Context\n- /Reliance/Jio is child of user's groups ✓
AuthZ -> AuthZ: Create Security Context\n{email, groupId: "/Reliance/Jio", groupRoles: {...}}

Gateway -> Service: Business Operation\nwith Security Context
Service -> GroupPerms: Check Authorization\nisAuthorized(sourceGroups, callerPermissions, requiredRoles, mode)
GroupPerms -> GroupPerms: Hierarchical Permission Check
GroupPerms --> Service: Authorization Result
Service --> Gateway: Operation Result
Gateway --> Client: Response

@enduml
```

## Key Components Description

### API Gateway Layer
- **API Gateway**: Central entry point for all requests
- **Load Balancer**: Distributes traffic across service instances

### Authentication & Authorization
- **JWT Token Service**: Validates and parses JWT tokens
- **AuthZ Service**: Handles authorization logic and security context
- **Cognito**: External identity provider for user authentication
- **Group Permissions**: Fine-grained access control based on group hierarchy

### Business Logic Layer
- **Group Service**: Manages group CRUD operations and hierarchy
- **User Service**: Handles user management and group membership
- **Security Utils**: Utility functions for role-based access control

### Data Access Layer
- **Group Repository**: Data access layer for group operations
- **User Repository**: Data access layer for user operations

### Data Storage
- **MongoDB**: Document database storing groups and users
- **Groups Collection**: Stores group hierarchy and metadata
- **Users Collection**: Stores user profiles and group memberships

## Security Principles

1. **Hierarchical Access Control**
   - Parent group admins can access child groups
   - Child group admins cannot access parent groups
   - Sibling groups have no cross-access

2. **Role-Based Permissions**
   - ADMIN: Full CRUD operations
   - CONTRIBUTOR: Read and limited write operations
   - READER: Read-only access

3. **Context Validation**
   - Group context must be within user's assigned groups
   - Operations are scoped to the current group context

4. **Audit and Compliance**
   - All operations are logged with user context
   - Immutable audit trail for compliance

## Scalability Considerations

- **Horizontal Scaling**: Services can be scaled independently
- **Caching**: Group hierarchy and permissions can be cached
- **Database Sharding**: Groups can be sharded by organization
- **Event-Driven Architecture**: Async processing for non-critical operations 