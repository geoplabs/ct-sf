# Groups Management System Documentation

This directory contains comprehensive documentation for the Groups Management System, including sequence diagrams, architecture overviews, and API documentation.

## 📋 Table of Contents

1. [System Overview](#system-overview)
2. [Documentation Index](#documentation-index)
3. [Quick Start Guide](#quick-start-guide)
4. [Key Concepts](#key-concepts)
5. [Security Model](#security-model)
6. [Use Cases](#use-cases)

## 🏗️ System Overview

The Groups Management System is a hierarchical access control system that supports:

- **Hierarchical Group Structure**: Parent companies, subsidiaries, and sub-divisions
- **Role-Based Access Control**: ADMIN, CONTRIBUTOR, and READER roles
- **Multi-Group User Management**: Users can belong to multiple groups with different roles
- **Fine-Grained Permissions**: Context-aware authorization based on group hierarchy
- **Audit Trail**: Complete logging of all operations for compliance

## 📚 Documentation Index

### Core Documentation
- **[groups-api.md](./groups-api.md)** - Complete API documentation with examples
- **[groups-sequence-diagram.md](./groups-sequence-diagram.md)** - Detailed sequence diagrams for all use cases
- **[groups-architecture-overview.md](./groups-architecture-overview.md)** - System architecture and component relationships

### Sequence Diagrams Covered

#### 1. Group Hierarchy Creation
- Creating parent companies with account IDs
- Adding subsidiaries and sub-divisions
- Hierarchical group ID generation

#### 2. User Management and Role Assignment
- User creation across different groups
- Role assignment and updates
- Multi-group user scenarios

#### 3. Role-Based Access Control
- ADMIN operations (full access)
- CONTRIBUTOR operations (limited write access)
- READER operations (read-only access)

#### 4. Hierarchical Access Control
- Parent admin accessing child groups
- Child admin restrictions
- Cross-subsidiary access controls

#### 5. Complex Scenarios
- Multi-group users with different roles
- Group deletion with dependency checks
- Security context switching

## 🚀 Quick Start Guide

### Understanding the Group Hierarchy

```
Root (/)
├── Parent Company (/Reliance)
│   ├── Subsidiary (/Reliance/Jio)
│   │   ├── Sub-division (/Reliance/Jio/JioFiber)
│   │   └── Sub-division (/Reliance/Jio/JioMobile)
│   ├── Subsidiary (/Reliance/Retail)
│   │   ├── Sub-division (/Reliance/Retail/Fashion)
│   │   └── Sub-division (/Reliance/Retail/Grocery)
│   └── Subsidiary (/Reliance/Energy)
└── Another Company (/TCS)
    ├── Division (/TCS/Consulting)
    └── Division (/TCS/Development)
```

### Role Hierarchy

| Role | Permissions | Can Create Groups | Can Manage Users | Can Access Child Groups |
|------|-------------|-------------------|------------------|-------------------------|
| **ADMIN** | Full CRUD | ✅ | ✅ | ✅ |
| **CONTRIBUTOR** | Read + Limited Write | ❌ | ❌ | ✅ (Read-only) |
| **READER** | Read-only | ❌ | ❌ | ✅ (Read-only) |

### Basic API Usage

```bash
# Create a parent company
POST /api/v1/groups
{
  "name": "Reliance",
  "isParentCompany": true,
  "description": "Energy and digital conglomerate"
}

# Create a subsidiary
POST /api/v1/groups
X-Group-Context: /Reliance
{
  "name": "Jio",
  "description": "Telecom subsidiary"
}

# Grant user access to a group
POST /api/v1/users
X-Group-Context: /Reliance/Jio
{
  "email": "john@reliance.com",
  "role": "ADMIN",
  "firstName": "John",
  "lastName": "Doe"
}
```

## 🔑 Key Concepts

### 1. Group Hierarchy
- Groups form a tree structure with parent-child relationships
- Group IDs follow the pattern: `/parent/child/grandchild`
- Root group (`/`) is the system-wide parent

### 2. Security Context
- Every API request operates within a group context
- Context is set via `X-Group-Context` header
- Context must be within user's assigned groups

### 3. Hierarchical Permissions
- Parent group admins can access child groups
- Child group admins cannot access parent groups
- Sibling groups have no cross-access by default

### 4. Multi-Group Users
- Users can belong to multiple groups
- Different roles can be assigned in each group
- Operations are scoped to the current context

## 🔒 Security Model

### Authentication Flow
1. User authenticates with Cognito
2. JWT token contains group memberships and roles
3. API Gateway validates token and extracts security context
4. Authorization service checks permissions for each operation

### Authorization Principles
- **Principle of Least Privilege**: Users get minimum required access
- **Context Validation**: Operations are scoped to valid group contexts
- **Hierarchical Control**: Parent groups control child group access
- **Role-Based Operations**: Each operation requires specific minimum roles

### Security Context Structure
```json
{
  "email": "user@example.com",
  "groupId": "/Reliance/Jio",
  "groupRoles": {
    "/Reliance": "READER",
    "/Reliance/Jio": "ADMIN",
    "/Reliance/Retail": "CONTRIBUTOR"
  }
}
```

## 📋 Use Cases

### Enterprise Scenarios
1. **Multi-National Corporation**: Parent company with regional subsidiaries
2. **Conglomerate Structure**: Diverse business units under one umbrella
3. **Franchise Model**: Central control with local autonomy
4. **Department Hierarchy**: Corporate departments and sub-departments

### Access Control Scenarios
1. **Executive Access**: C-level executives with broad access across subsidiaries
2. **Department Managers**: Limited access to their department and sub-departments
3. **Team Leads**: Access to specific teams within departments
4. **Individual Contributors**: Access to their immediate team only

### Operational Scenarios
1. **Onboarding**: Adding new users to appropriate groups with correct roles
2. **Reorganization**: Moving users between groups during restructuring
3. **Offboarding**: Removing user access while maintaining audit trail
4. **Compliance**: Ensuring proper access controls for regulatory requirements

## 🔧 Implementation Notes

### Database Design
- Groups and Users are stored in separate MongoDB collections
- Group hierarchy is maintained through ID structure
- User-group relationships are stored as embedded documents

### Performance Considerations
- Group hierarchy queries are optimized with proper indexing
- Permission checks use efficient tree traversal algorithms
- Caching is implemented for frequently accessed group structures

### Scalability Features
- Horizontal scaling of service components
- Database sharding by organization
- Async processing for non-critical operations
- Event-driven architecture for real-time updates

## 📖 Additional Resources

- **API Reference**: See [groups-api.md](./groups-api.md) for complete API documentation
- **Sequence Diagrams**: See [groups-sequence-diagram.md](./groups-sequence-diagram.md) for detailed interaction flows
- **Architecture**: See [groups-architecture-overview.md](./groups-architecture-overview.md) for system design details

## 🤝 Contributing

When contributing to the Groups Management System:

1. Follow the established security principles
2. Ensure all operations maintain audit trails
3. Test hierarchical permission scenarios thoroughly
4. Update documentation for any API changes
5. Include sequence diagrams for new use cases

---

*For technical support or questions about the Groups Management System, please refer to the detailed documentation files or contact the development team.* 