# Jackson View Extension Spring Boot Starter

[![Maven Central](https://img.shields.io/maven-central/v/io.github.vennarshulytz/json-view-ext-spring-boot-starter.svg)](https://search.maven.org/artifact/io.github.vennarshulytz/json-view-ext-spring-boot-starter)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/Java-8%2B-orange.svg)](https://www.oracle.com/java/)

##### 📖 English Documentation | 📖 [中文文档](README_zh.md)

A Spring Boot Starter based on Jackson that provides more flexible JSON serialization field control than `@JsonView`, supporting fine-grained field filtering and sensitive data masking.

## 📖 Introduction

In Spring Boot projects, data returned from the Controller layer often needs to be customized for different scenarios. While the traditional `@JsonView` annotation can achieve view control, it is not flexible enough for complex scenarios. This project provides a more powerful solution:

- **Fine-grained field control**: Support precise control of serialization fields by type and path
- **Nested path support**: Locate specific fields in nested objects using `.` separator
- **Sensitive data masking**: Built-in masking processors with custom extension support
- **Priority mechanism**: include has higher priority than exclude, with clear rules
- **Zero-intrusion design**: No need to modify existing entity classes, just add annotations to Controller methods
- **Multi-version support**: Compatible with Spring Boot 1.x, 2.x, and 3.x

## 🎯 Background

During our company's project upgrade, we found that returning full data from the Controller layer has the following issues:

1. **Performance impact**: Large amounts of useless fields increase network transmission overhead
2. **Security risks**: Sensitive information may be accidentally exposed
3. **Maintenance cost**: Refactoring with VO entities is too tedious

To solve these problems, we developed this project as a transitional solution, allowing you to:

- **Quickly control** returned fields without creating numerous VO classes
- **Smoothly transition** to standard VO pattern, reducing refactoring risks
- **Flexibly configure** return content for different APIs

### 🔌 Recommended: FastConvert IDEA Plugin

For future refactoring to VO entities, we recommend using our IDEA plugin **[FastConvert](https://plugins.jetbrains.com/plugin/28433-fastconvert)**, which helps you:

- Generate object conversion code with one click
- Intelligently match field mappings
- More convenient and efficient than `BeanUtils` or `MapStruct`

👉 [Plugin Details](https://plugins.jetbrains.com/plugin/28433-fastconvert/about)

##  🔄 Version Compatibility

| Starter Module                       | Spring Boot | JDK  | Servlet API |
| ------------------------------------ | ----------- | ---- | ----------- |
| `json-view-ext-spring-boot-starter`  | 1.x / 2.x   | 8+   | javax       |
| `json-view-ext-spring-boot3-starter` | 3.x         | 17+  | jakarta     |

## 🚀 Quick Start

### 1. Add Dependency

#### Spring Boot 1.x / Spring Boot 2.x（JDK 8+）

**Maven:**

```xml
<dependency>
    <groupId>io.github.vennarshulytz</groupId>
    <artifactId>json-view-ext-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Gradle:**
```groovy
implementation 'io.github.vennarshulytz:json-view-ext-spring-boot-starter:1.0.0'
```

#### Spring Boot 3.x（JDK 17+）

**Maven:**

```xml
<dependency>
    <groupId>io.github.vennarshulytz</groupId>
    <artifactId>json-view-ext-spring-boot3-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Gradle:**

```groovy
implementation 'io.github.vennarshulytz:json-view-ext-spring-boot3-starter:2.0.0'
```

### 2. Enable Feature

Add `@EnableJsonViewExt` annotation to your Spring Boot application class:

```java
@SpringBootApplication
@EnableJsonViewExt
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

## 📚 Usage Guide

### Entity Classes

The following entity classes are used in the examples:

```java
@Data
public class Department {
    private String name;
    private Employee manager1;
    private List<Employee> managerList1;
}

@Data
public class Employee {
    private String id;
    private String name;
    private String number;
    private Address address1;
    private List<Address> addressList1;
}

@Data
public class Address {
    private String id;
    private String province;
    private String city;
}
```

---

### Basic Usage

#### Scenario 1: Include - Only Include Specified Fields

Return only `name` and `number` fields of `Employee` class:

```java
@GetMapping("/employee")
@JsonViewExt(
    include = {
        @JsonFilterExt(clazz = Employee.class, props = {"name", "number"})
    }
)
public Employee getEmployee() {
    return employeeService.findById("1");
}
```

**Response without annotation:**
```json
{
    "id": "9ccb342f-18f2-46c1-872e-892c0c3f6d14",
    "name": "John",
    "number": "EMP001",
    "address1": {
        "id": "a7ab2748-68ec-4e52-97c3-a241317ef347",
        "province": "California",
        "city": "Los Angeles"
    },
    "addressList1": [
        {
            "id": "c3b735e8-c8c7-4aab-b8e0-fddab5bcbe2e",
            "province": "California",
            "city": "San Francisco"
        }
    ]
}
```

**Response with annotation:**
```json
{
    "name": "John",
    "number": "EMP001"
}
```

---

#### Scenario 2: Exclude - Exclude Specified Fields

Exclude `id` field when returning `Address` class:

```java
@GetMapping("/address")
@JsonViewExt(
    exclude = {
        @JsonFilterExt(clazz = Address.class, props = {"id"})
    }
)
public Address getAddress() {
    return addressService.findById("1");
}
```

**Response without annotation:**
```json
{
    "id": "a7ab2748-68ec-4e52-97c3-a241317ef347",
    "province": "California",
    "city": "Los Angeles"
}
```

**Response with annotation:**
```json
{
    "province": "California",
    "city": "Los Angeles"
}
```

---

#### Scenario 3: Multi-type Combination Control

Control serialization fields for multiple types simultaneously:

```java
@GetMapping("/department")
@JsonViewExt(
    include = {
        @JsonFilterExt(clazz = Department.class, props = {"name", "manager1"}),
        @JsonFilterExt(clazz = Employee.class, props = {"name", "number"})
    },
    exclude = {
        @JsonFilterExt(clazz = Address.class, props = {"id"})
    }
)
public Department getDepartment() {
    return departmentService.findById("1");
}
```

**Response without annotation:**
```json
{
    "name": "Engineering",
    "manager1": {
        "id": "emp-001",
        "name": "John",
        "number": "EMP001",
        "address1": {
            "id": "addr-001",
            "province": "California",
            "city": "Los Angeles"
        },
        "addressList1": []
    },
    "managerList1": []
}
```

**Response with annotation:**
```json
{
    "name": "Engineering",
    "manager1": {
        "name": "John",
        "number": "EMP001"
    }
}
```

---

### Advanced Usage

#### Scenario 1: Field Path Matching - Target Specific Locations

Use the `field` property to precisely control serialization of objects at specific paths:

```java
@GetMapping("/department/detail")
@JsonViewExt(
    include = {
        @JsonFilterExt(clazz = Department.class, props = {"manager1", "managerList1"}),
        // For Employee in managerList1, only return name and address1
        @JsonFilterExt(clazz = Employee.class, field = "managerList1", props = {"name", "address1"}),
        // For Employee at other locations (e.g., manager1), return name and number
        @JsonFilterExt(clazz = Employee.class, props = {"name", "number"}),
        // For Address at managerList1.address1 path, only return province
        @JsonFilterExt(clazz = Address.class, field = "managerList1.address1", props = {"province"}),
        // For Address at other locations, return province and city
        @JsonFilterExt(clazz = Address.class, props = {"province", "city"})
    }
)
public Department getDepartmentDetail() {
    return departmentService.findById("1");
}
```

**Result explanation:**
- `manager1` (Employee type): returns `name`, `number`
- Employee in `managerList1`: returns `name`, `address1`
- `managerList1[*].address1`: only returns `province`
- Other Address: returns `province`, `city`

---

#### Scenario 2: Sensitive Data Masking

Use the `sensitives` property to mask sensitive fields:

```java
@GetMapping("/employee/info")
@JsonViewExt(
    include = {
        @JsonFilterExt(
            clazz = Employee.class,
            props = {"name", "number", "address1"},
            sensitives = {
                @Sensitive(type = IdCardType.class, props = {"number"})
            }
        )
    }
)
public Employee getEmployeeInfo() {
    return employeeService.findById("1");
}
```

**Response without annotation:**
```json
{
    "id": "emp-001",
    "name": "John",
    "number": "123456789012345678",
    "address1": {
        "id": "addr-001",
        "province": "California",
        "city": "Los Angeles"
    },
    "addressList1": []
}
```

**Response with annotation:**
```json
{
    "name": "John",
    "number": "123456********5678",
    "address1": {
        "id": "addr-001",
        "province": "California",
        "city": "Los Angeles"
    }
}
```

---

#### Scenario 3: Different Masking Rules for Same Type at Different Paths

```java
@GetMapping("/department/sensitive")
@JsonViewExt(
    include = {
        @JsonFilterExt(clazz = Department.class, props = {"manager1", "managerList1"}),
        // Employee numbers in managerList1 need masking
        @JsonFilterExt(
            clazz = Employee.class,
            field = "managerList1",
            props = {"name", "number"},
            sensitives = {@Sensitive(type = IdCardType.class, props = {"number"})}
        ),
        // manager1's employee number doesn't need masking
        @JsonFilterExt(clazz = Employee.class, props = {"name", "number"})
    }
)
public Department getDepartmentSensitive() {
    return departmentService.findById("1");
}
```

**Response:**
```json
{
    "manager1": {
        "name": "John",
        "number": "123456789012345678"
    },
    "managerList1": [
        {
            "name": "Jane",
            "number": "123456********5679"
        },
        {
            "name": "Bob",
            "number": "123456********5670"
        }
    ]
}
```

---

#### Scenario 4: Complete Complex Example

```java
@GetMapping("/findById")
@JsonViewExt(
    include = {
        @JsonFilterExt(clazz = Department.class, props = {"manager1", "managerList1"}),
        @JsonFilterExt(
            clazz = Employee.class,
            field = "managerList1",
            props = {"name", "number", "address1"},
            sensitives = {@Sensitive(type = IdCardType.class, props = {"number"})}
        ),
        @JsonFilterExt(clazz = Employee.class, props = {"number", "address1", "addressList1"}),
        @JsonFilterExt(clazz = Address.class, field = "managerList1.address1", props = {"id", "province"})
    },
    exclude = {
        @JsonFilterExt(clazz = Address.class, props = {"id"})
    }
)
public Department findById(@RequestParam("id") String id) {
    return departmentService.findById(id);
}
```

**Response without annotation:**
```json
{
    "name": "name",
    "manager1": {
        "id": "9ccb342f-18f2-46c1-872e-892c0c3f6d14",
        "name": "name1",
        "number": "123456789012345678",
        "address1": {
            "id": "a7ab2748-68ec-4e52-97c3-a241317ef347",
            "province": "province1",
            "city": "city1"
        },
        "addressList1": [
            {"id": "c3b735e8-c8c7-4aab-b8e0-fddab5bcbe2e", "province": "province2", "city": "city2"},
            {"id": "e8f30e87-53b4-47f8-ba5a-1b49b6bca3a2", "province": "province3", "city": "city3"}
        ]
    },
    "managerList1": [
        {
            "id": "19cdb3dc-3877-4a45-a296-c71fae143040",
            "name": "name2",
            "number": "123456789012345679",
            "address1": {
                "id": "d724f421-a78a-494e-87d1-5f2e1622e3ba",
                "province": "province4",
                "city": "city4"
            },
            "addressList1": [
                {"id": "f9d53824-9f98-4eec-ba84-708113c804d8", "province": "province5", "city": "city5"},
                {"id": "22f0997d-9bc5-4415-bdd1-63d302c629ce", "province": "province6", "city": "city6"}
            ]
        },
        {
            "id": "e9e99b53-14cf-42d6-a70b-1595517a6671",
            "name": "name3",
            "number": "123456789012345670",
            "address1": {
                "id": "8fe894dd-d61d-4821-ae13-5bddd54839dc",
                "province": "province7",
                "city": "city7"
            },
            "addressList1": [
                {"id": "4dbb776a-22d0-4222-9c0b-83d7391a3eec", "province": "province8", "city": "city8"},
                {"id": "83c545ce-c7fd-4339-8c62-4ae3ceda32ec", "province": "province9", "city": "city9"}
            ]
        }
    ]
}
```

**Response with annotation:**
```json
{
    "manager1": {
        "number": "123456789012345678",
        "address1": {
            "province": "province1",
            "city": "city1"
        },
        "addressList1": [
            {"province": "province2", "city": "city2"},
            {"province": "province3", "city": "city3"}
        ]
    },
    "managerList1": [
        {
            "name": "name2",
            "number": "123456********5679",
            "address1": {
                "id": "d724f421-a78a-494e-87d1-5f2e1622e3ba",
                "province": "province4"
            }
        },
        {
            "name": "name3",
            "number": "123456********5670",
            "address1": {
                "id": "8fe894dd-d61d-4821-ae13-5bddd54839dc",
                "province": "province7"
            }
        }
    ]
}
```

---

### Custom Masking Processor

The project includes built-in masking processors such as `IdCardType` (ID card) and `PhoneType` (phone number). To customize masking rules, simply implement the `SensitiveType` interface:

```java
/**
 * Custom bank card number masking processor
 */
public class BankCardType implements SensitiveType {

    @Override
    public String desensitize(String value) {
        if (value == null || value.length() < 8) {
            return value;
        }
        // Keep first 4 and last 4 digits, replace middle with *
        return value.substring(0, 4)
            + "****"
            + "****"
            + "****"
            + value.substring(value.length() - 4);
    }
}
```

Using custom masking processor:

```java
@JsonViewExt(
    include = {
        @JsonFilterExt(
            clazz = Account.class,
            props = {"bankCard", "balance"},
            sensitives = {@Sensitive(type = BankCardType.class, props = {"bankCard"})}
        )
    }
)
```

---

## 📋 Rule Reference

| Rule | Description |
|------|-------------|
| include has higher priority than exclude | When both include and exclude are configured, include rules take precedence |
| field exact match takes priority | Rules with field take priority over generic rules without field |
| Later definitions override earlier ones | For multiple rules with same clazz and field, the later one takes effect |
| Nested paths use `.` separator | e.g., `managerList1.address1` means the address1 property under managerList1 |

## 🧱 Module Structure

```
json-view-ext-spring-boot-starter/
├── json-view-ext-core                          # Core module
├── json-view-ext-spring-boot-starter-javax     # Spring Boot 1.x / Spring Boot 2.x support  (JDK 8+)
└── json-view-ext-spring-boot3-starter-jakarta  # Spring Boot 3.x support (JDK 17+)
```

## 🤝 Contributing

Issues and Pull Requests are welcome!

## 📄 License

This project is licensed under the [Apache License 2.0](LICENSE).

---

