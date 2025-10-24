# Groups API Documentation

This document provides comprehensive examples and guidelines for interacting with the Groups API.

## Table of Contents

- [Authentication](#authentication)
- [API Endpoints](#api-endpoints)
  - [List Groups](#list-groups)
  - [Get Group](#get-group)
  - [Create Group](#create-group)
  - [Update Group](#update-group)
  - [Delete Group](#delete-group)
- [Complex Tag Structure](#complex-tag-structure)
- [Error Handling](#error-handling)

## Authentication

All API requests require authentication. Include your authentication token in the request header:

```
Authorization: Bearer your-jwt-token
```

Optionally, you can set the following context headers:

```
X-Group-Context: group-id
X-User-Email: user@example.com
```

## API Endpoints

Base URL: `/api/v1/groups`

### List Groups

Retrieve a paginated list of groups with optional filtering by tags.

**Endpoint:** `GET /api/v1/groups`

**Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `fromGroupId` | String | Token for pagination (optional) |
| `count` | Integer | Number of results per page (default: 10) |
| `includeChildGroups` | Boolean | Include child groups (default: false) |
| `includeParentGroups` | Boolean | Include parent groups (default: false) |
| `tags.*` | String | Filter by tags (e.g., tags.priority=high) |

**Example Request:**

```bash
GET /api/v1/groups?count=5&tags.priority=high
```

**Example Response:**

```json
{
  "groups": [
    {
      "id": "/Reliance",
      "name": "Reliance Industries",
      "description": "Energy and petrochemicals conglomerate",
      "state": "ACTIVE",
      "tags": {
        "priority": "high",
        "industryClassification": {
          "codes": {
            "naics": [
              { "value": "324110" }
            ]
          }
        }
      },
      "createdBy": "admin@example.com",
      "createdAt": "2023-05-20T10:30:00Z",
      "updatedBy": "admin@example.com",
      "updatedAt": "2023-06-15T14:22:30Z"
    },
    {
      "id": "/TCS",
      "name": "Tata Consultancy Services",
      "description": "IT services and consulting",
      "state": "ACTIVE",
      "tags": {
        "priority": "high",
        "industryClassification": {
          "codes": {
            "naics": [
              { "value": "541512" }
            ]
          }
        }
      }
    }
  ],
  "pagination": {
    "lastEvaluatedToken": "TCS"
  }
}
```

### Get Group

Retrieve a single group by ID.

**Endpoint:** `GET /api/v1/groups/{groupId}`

**Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `showConfigurationSource` | Boolean | Include configuration source (default: false) |

**Example Request:**

```bash
GET /api/v1/groups/Reliance
```

**Example Response:**

```json
{
  "id": "/Reliance",
  "name": "Reliance Industries",
  "description": "Energy and petrochemicals conglomerate",
  "state": "ACTIVE",
  "tags": {
    "industryClassification": {
      "codes": {
        "naics": [
          { "value": "324110" }
        ],
        "sic": [
          { "value": "2911" }
        ]
      },
      "primary": {
        "taxonomy": { "value": "naics" },
        "code": { "value": "324110" }
      }
    },
    "priority": { "value": "high" }
  },
  "createdBy": "admin@example.com",
  "createdAt": "2023-05-20T10:30:00Z",
  "updatedBy": "admin@example.com",
  "updatedAt": "2023-06-15T14:22:30Z"
}
```

### Create Group

Create a new group.

**Endpoint:** `POST /api/v1/groups`

**Request Body:**

```json
{
  "name": "Tesla",
  "description": "Electric vehicles and clean energy",
  "tags": {
    "industryClassification": {
      "codes": {
        "naics": [
          { "value": "336111" }
        ],
        "sic": [
          { "value": "3711" }
        ]
      },
      "primary": {
        "taxonomy": { "value": "naics" },
        "code": { "value": "336111" }
      }
    },
    "priority": { "value": "medium" }
  }
}
```

**Example Response:**

```json
{
  "id": "/Tesla",
  "name": "Tesla",
  "description": "Electric vehicles and clean energy",
  "state": "ACTIVE",
  "tags": {
    "industryClassification": {
      "codes": {
        "naics": [
          { "value": "336111" }
        ],
        "sic": [
          { "value": "3711" }
        ]
      },
      "primary": {
        "taxonomy": { "value": "naics" },
        "code": { "value": "336111" }
      }
    },
    "priority": { "value": "medium" }
  },
  "createdBy": "admin@example.com",
  "createdAt": "2023-07-01T15:30:00Z",
  "updatedBy": "admin@example.com",
  "updatedAt": "2023-07-01T15:30:00Z"
}
```

### Update Group

Update an existing group.

**Endpoint:** `PATCH /api/v1/groups/{groupId}`

**Example Request:**

```bash
PATCH /api/v1/groups/Reliance
```

**Request Body (with complex tags):**

```json
{
  "description": "Leading energy and digital conglomerate",
  "tags": {
    "industryClassification": {
      "codes": {
        "naics": [
          { "value": "111110" },
          { "value": "313000" }
        ],
        "sic": [
          { "value": "2911" }
        ],
        "isic": [
          { "value": "1920" },
          { "value": "0111" }
        ],
        "gics": [
          { "value": "101020" },
          { "value": "151040" }
        ],
        "nace": [
          { "value": "A01.11" },
          { "value": "C19.2" }
        ]
      },
      "primary": {
        "taxonomy": { "value": "naics" },
        "code": { "value": "111110" }
      }
    },
    "businessDescription": { "value": "Leading energy conglomerate with diversified operations" },
    "operations": {
      "regions": [
        {
          "name": { "value": "Americas" },
          "countries": [
            { "value": "Antigua and Barbuda" },
            { "value": "Argentina" }
          ]
        },
        {
          "name": { "value": "Asia" },
          "countries": [
            { "value": "India" }
          ]
        }
      ],
      "headquarters": {
        "streetAddress": { "value": "Maker Chambers IV" },
        "city": { "value": "Mumbai" },
        "country": { "value": "India" },
        "latitude": { "value": "19.0176147" },
        "longitude": { "value": "72.8561644" }
      }
    },
    "reportingBaseline": {
      "useCalendarYear": { "value": true },
      "customFiscalYear": {
        "start": { "value": null },
        "end": { "value": null }
      },
      "baselineYear": { "value": 2022 },
      "baselineAdjustmentNotes": { "value": "" },
      "unitSystem": { "value": "metric" },
      "gwpHorizon": { "value": "AR4_100yr" }
    },
    "targetsCommitments": {
      "netZeroCommitted": { "value": false },
      "sbtiCommitted": { "value": false },
      "targetType": { "value": "absolute" },
      "targetYear": { "value": 2030 },
      "reductionPercentage": { "value": 30 }
    },
    "governance": {
      "sustainabilityLead": {
        "name": { "value": "John Doe" },
        "email": { "value": "john.doe@reliance.com" },
        "phone": { "value": "+91-22-12345678" }
      },
      "financeLead": {
        "name": { "value": "Jane Smith" },
        "email": { "value": "jane.smith@reliance.com" },
        "phone": { "value": "+91-22-87654321" }
      },
      "boardOversight": { "value": false }
    },
    "securityPreferences": {
      "ssoDomains": [
        { "value": "reliance.com" }
      ],
      "dataResidencyRegion": { "value": "ap-south-1" },
      "adminUsers": [
        { "value": "admin1@reliance.com" }
      ]
    },
    "legalConsent": {
      "dpaAccepted": { "value": true },
      "retentionPeriodYears": { "value": 10 }
    },
    "priority": { "value": "high" }
  }
}
```

**Example Response:**

```json
{
  "id": "/Reliance",
  "name": "Reliance Industries",
  "description": "Leading energy and digital conglomerate",
  "state": "ACTIVE",
  "tags": {
    "industryClassification": {
      "codes": {
        "naics": [
          { "value": "111110" },
          { "value": "313000" }
        ],
        "sic": [
          { "value": "2911" }
        ],
        "isic": [
          { "value": "1920" },
          { "value": "0111" }
        ],
        "gics": [
          { "value": "101020" },
          { "value": "151040" }
        ],
        "nace": [
          { "value": "A01.11" },
          { "value": "C19.2" }
        ]
      },
      "primary": {
        "taxonomy": { "value": "naics" },
        "code": { "value": "111110" }
      }
    },
    "businessDescription": { "value": "Leading energy conglomerate with diversified operations" },
    "operations": {
      "regions": [
        {
          "name": { "value": "Americas" },
          "countries": [
            { "value": "Antigua and Barbuda" },
            { "value": "Argentina" }
          ]
        },
        {
          "name": { "value": "Asia" },
          "countries": [
            { "value": "India" }
          ]
        }
      ],
      "headquarters": {
        "streetAddress": { "value": "Maker Chambers IV" },
        "city": { "value": "Mumbai" },
        "country": { "value": "India" },
        "latitude": { "value": "19.0176147" },
        "longitude": { "value": "72.8561644" }
      }
    },
    "reportingBaseline": {
      "useCalendarYear": { "value": true },
      "customFiscalYear": {
        "start": { "value": null },
        "end": { "value": null }
      },
      "baselineYear": { "value": 2022 },
      "baselineAdjustmentNotes": { "value": "" },
      "unitSystem": { "value": "metric" },
      "gwpHorizon": { "value": "AR4_100yr" }
    },
    "targetsCommitments": {
      "netZeroCommitted": { "value": false },
      "sbtiCommitted": { "value": false },
      "targetType": { "value": "absolute" },
      "targetYear": { "value": 2030 },
      "reductionPercentage": { "value": 30 }
    },
    "governance": {
      "sustainabilityLead": {
        "name": { "value": "John Doe" },
        "email": { "value": "john.doe@reliance.com" },
        "phone": { "value": "+91-22-12345678" }
      },
      "financeLead": {
        "name": { "value": "Jane Smith" },
        "email": { "value": "jane.smith@reliance.com" },
        "phone": { "value": "+91-22-87654321" }
      },
      "boardOversight": { "value": false }
    },
    "securityPreferences": {
      "ssoDomains": [
        { "value": "reliance.com" }
      ],
      "dataResidencyRegion": { "value": "ap-south-1" },
      "adminUsers": [
        { "value": "admin1@reliance.com" }
      ]
    },
    "legalConsent": {
      "dpaAccepted": { "value": true },
      "retentionPeriodYears": { "value": 10 }
    },
    "priority": { "value": "high" }
  },
  "createdBy": "admin@example.com",
  "createdAt": "2023-05-20T10:30:00Z",
  "updatedBy": "admin@example.com",
  "updatedAt": "2023-07-01T16:42:15Z"
}
```

### Delete Group

Delete a group by ID.

**Endpoint:** `DELETE /api/v1/groups/{groupId}`

**Example Request:**

```bash
DELETE /api/v1/groups/Tesla
```

**Example Response:**

```json
{
  "message": "Group successfully deleted",
  "groupId": "/Tesla"
}
```

## Complex Tag Structure

The Groups API supports a flexible and extensible tag structure. Tags can include nested objects, arrays, and primitive values.

### Tag Value Wrapper

For most values, we recommend using the `{ "value": ... }` wrapper to maintain consistency and allow for metadata in the future:

```json
"businessDescription": { "value": "Leading energy conglomerate" }
```

### Supported Tag Sections

The following tag sections are supported with suggested structures:

#### Industry Classification

```json
"industryClassification": {
  "codes": {
    "naics": [
      { "value": "111110" },
      { "value": "313000" }
    ],
    "sic": [
      { "value": "2911" }
    ],
    "isic": [
      { "value": "1920" }
    ],
    "gics": [
      { "value": "101020" }
    ],
    "nace": [
      { "value": "A01.11" }
    ]
  },
  "primary": {
    "taxonomy": { "value": "naics" },
    "code": { "value": "111110" }
  }
}
```

#### Operations

```json
"operations": {
  "regions": [
    {
      "name": { "value": "Americas" },
      "countries": [
        { "value": "United States" },
        { "value": "Canada" }
      ]
    }
  ],
  "headquarters": {
    "streetAddress": { "value": "123 Main St" },
    "city": { "value": "New York" },
    "country": { "value": "United States" },
    "latitude": { "value": "40.7128" },
    "longitude": { "value": "-74.0060" }
  }
}
```

#### Reporting & Baseline

```json
"reportingBaseline": {
  "useCalendarYear": { "value": true },
  "customFiscalYear": {
    "start": { "value": null },
    "end": { "value": null }
  },
  "baselineYear": { "value": 2022 },
  "baselineAdjustmentNotes": { "value": "" },
  "unitSystem": { "value": "metric" },
  "gwpHorizon": { "value": "AR4_100yr" }
}
```

#### Targets & Commitments

```json
"targetsCommitments": {
  "netZeroCommitted": { "value": false },
  "sbtiCommitted": { "value": false },
  "targetType": { "value": "absolute" },
  "targetYear": { "value": 2030 },
  "reductionPercentage": { "value": 30 }
}
```

#### Governance

```json
"governance": {
  "sustainabilityLead": {
    "name": { "value": "John Doe" },
    "email": { "value": "john.doe@example.com" },
    "phone": { "value": "+1-555-123-4567" }
  },
  "financeLead": {
    "name": { "value": "Jane Smith" },
    "email": { "value": "jane.smith@example.com" },
    "phone": { "value": "+1-555-765-4321" }
  },
  "boardOversight": { "value": false }
}
```

#### Security & Preferences

```json
"securityPreferences": {
  "ssoDomains": [
    { "value": "example.com" }
  ],
  "dataResidencyRegion": { "value": "us-east-1" },
  "adminUsers": [
    { "value": "admin@example.com" }
  ]
}
```

#### Legal & Consent

```json
"legalConsent": {
  "dpaAccepted": { "value": true },
  "retentionPeriodYears": { "value": 7 }
}
```

## Error Handling

The API returns standard HTTP status codes:

- `200 OK`: Successful operation
- `201 Created`: Resource successfully created
- `400 Bad Request`: Invalid request format or parameters
- `401 Unauthorized`: Missing or invalid authentication
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: Resource not found
- `500 Internal Server Error`: Server-side error

Error responses include a descriptive message:

```json
{
  "status": 400,
  "message": "Invalid request format",
  "details": "Cannot deserialize value of type `java.lang.String` from Object value"
}
```

## Using cURL

Example cURL command to update a group with complex tags:

```bash
curl -X PATCH \
  http://localhost:8080/api/v1/groups/Reliance \
  -H 'Authorization: Bearer your-jwt-token' \
  -H 'Content-Type: application/json' \
  -d '{
  "description": "Leading energy and digital conglomerate",
  "tags": {
    "industryClassification": {
      "codes": {
        "naics": [
          { "value": "111110" }
        ]
      },
      "primary": {
        "taxonomy": { "value": "naics" },
        "code": { "value": "111110" }
      }
    },
    "priority": { "value": "high" }
  }
}' 