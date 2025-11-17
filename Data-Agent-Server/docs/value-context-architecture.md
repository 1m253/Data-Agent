# Value Context Architecture

## Overview

The Value Context architecture provides a unified abstraction for value extraction and type conversion across different data source types (JDBC, MongoDB, Redis, Elasticsearch, etc.).

## Architecture Design

### Core Components

```
ValueContext (Interface)
├── JdbcValueContext (JDBC implementation)
├── MongoValueContext (MongoDB implementation)
├── RedisValueContext (Redis implementation)
└── ElasticsearchValueContext (Future)

ValueProcessor (Interface)
├── DefaultValueProcessor (Base implementation)
├── MySQLValueProcessor (MySQL-specific)
├── PostgreSQLValueProcessor (Future)
├── MongoValueProcessor (Future)
└── RedisValueProcessor (Future)

Factory Classes
├── JdbcValueContextFactory (JDBC context factory)
├── MongoValueContextFactory (Future)
└── RedisValueContextFactory (Future)
```

## Design Principles

### 1. **Abstraction over Implementation**

The `ValueContext` interface provides a common abstraction that works for any data source:

```java
public interface ValueContext {
    String getDataSourceType();  // "JDBC", "MONGODB", "REDIS", etc.
    int getIndex();               // Column/field index
    String getName();             // Column/field name
    String getTypeName();         // Type name (SQL type, BSON type, etc.)
    Object getMetadata(String key); // Additional metadata
    boolean isNullable();         // Nullable check
}
```

### 2. **Separation of Concerns**

- **ValueContext**: Data container (what to process)
- **ValueProcessor**: Processing logic (how to process)
- **Factory**: Creation logic (how to create context)

### 3. **Backward Compatibility**

The `ValueProcessor` interface maintains backward compatibility:

```java
public interface ValueProcessor {
    // New generic method
    default Object getValue(ValueContext context) throws Exception {
        if (context instanceof JdbcValueContext jdbcContext) {
            return getJdbcValue(jdbcContext);
        }
        throw new UnsupportedOperationException(...);
    }
    
    // Old JDBC-specific method (still supported)
    Object getJdbcValue(JdbcValueContext context) throws SQLException;
}
```

## Usage Examples

### JDBC Usage

```java
// Create context using factory
JdbcValueContext context = JdbcValueContextFactory.fromMetaData(resultSet, metaData, columnIndex);

// Process value
ValueProcessor processor = new MySQLValueProcessor();
Object value = processor.getJdbcValue(context);

// Or use generic method
Object value = processor.getValue(context);
```

### MongoDB Usage (Future)

```java
// Create MongoDB context
MongoValueContext context = MongoValueContext.builder()
    .document(mongoDocument)
    .fieldName("username")
    .fieldIndex(0)
    .bsonTypeName("String")
    .build();

// Process value
ValueProcessor processor = new MongoValueProcessor();
Object value = processor.getValue(context);
```

### Redis Usage (Future)

```java
// Create Redis context
RedisValueContext context = RedisValueContext.builder()
    .key("user:1001")
    .fieldName("name")
    .redisType("HASH")
    .value(redisValue)
    .build();

// Process value
ValueProcessor processor = new RedisValueProcessor();
Object value = processor.getValue(context);
```

## Implementation Guide

### Adding a New Data Source

#### Step 1: Create ValueContext Implementation

```java
@Data
@Builder
public class ElasticsearchValueContext implements ValueContext {
    private Object document;
    private String fieldName;
    private int fieldIndex;
    private String esTypeName;
    private Map<String, Object> additionalMetadata = new HashMap<>();
    
    @Override
    public String getDataSourceType() {
        return "ELASTICSEARCH";
    }
    
    @Override
    public int getIndex() {
        return fieldIndex;
    }
    
    @Override
    public String getName() {
        return fieldName;
    }
    
    @Override
    public String getTypeName() {
        return esTypeName;
    }
    
    @Override
    public Object getMetadata(String key) {
        return additionalMetadata.get(key);
    }
}
```

#### Step 2: Create Factory Class

```java
public class ElasticsearchValueContextFactory {
    public static ElasticsearchValueContext fromDocument(
            Object document, String fieldName, int fieldIndex) {
        return ElasticsearchValueContext.builder()
            .document(document)
            .fieldName(fieldName)
            .fieldIndex(fieldIndex)
            .esTypeName(detectType(document, fieldName))
            .build();
    }
}
```

#### Step 3: Create ValueProcessor Implementation

```java
public class ElasticsearchValueProcessor implements ValueProcessor {
    @Override
    public Object getValue(ValueContext context) throws Exception {
        if (!(context instanceof ElasticsearchValueContext esContext)) {
            throw new UnsupportedOperationException(
                "ElasticsearchValueProcessor only supports ElasticsearchValueContext");
        }
        
        // Extract and convert value from Elasticsearch document
        Object document = esContext.getDocument();
        String fieldName = esContext.getName();
        // ... conversion logic
        
        return convertedValue;
    }
    
    @Override
    public Object getJdbcValue(JdbcValueContext context) throws SQLException {
        throw new UnsupportedOperationException(
            "ElasticsearchValueProcessor does not support JDBC");
    }
    
    @Override
    public boolean supports(ValueContext context) {
        return context instanceof ElasticsearchValueContext;
    }
}
```

## Benefits

### 1. **Extensibility**
- Easy to add new data source types
- No need to modify existing code

### 2. **Type Safety**
- Compile-time type checking
- Clear interface contracts

### 3. **Flexibility**
- Each data source can have its own metadata
- Generic abstraction works for all types

### 4. **Maintainability**
- Clear separation of concerns
- Easy to test each component independently

### 5. **Backward Compatibility**
- Existing JDBC code continues to work
- Gradual migration path

## Migration Guide

### For Existing JDBC Code

No changes required! The old API still works:

```java
// Old code (still works)
JdbcValueContext context = JdbcValueContextFactory.fromMetaData(rs, meta, i);
Object value = processor.getJdbcValue(context);
```

### For New Code

Use the generic API:

```java
// New code (recommended)
ValueContext context = JdbcValueContextFactory.fromMetaData(rs, meta, i);
Object value = processor.getValue(context);
```

## Future Enhancements

### 1. **Context Pooling**
Reuse context objects to reduce GC pressure:

```java
public class ValueContextPool {
    public static ValueContext acquire(String type) { ... }
    public static void release(ValueContext context) { ... }
}
```

### 2. **Async Processing**
Support asynchronous value processing:

```java
public interface AsyncValueProcessor extends ValueProcessor {
    CompletableFuture<Object> getValueAsync(ValueContext context);
}
```

### 3. **Batch Processing**
Process multiple values in one call:

```java
public interface BatchValueProcessor extends ValueProcessor {
    List<Object> getValues(List<ValueContext> contexts);
}
```

### 4. **Type Conversion Pipeline**
Chain multiple processors:

```java
ValueProcessor pipeline = ValueProcessorPipeline.builder()
    .add(new TypeDetectionProcessor())
    .add(new ValidationProcessor())
    .add(new ConversionProcessor())
    .build();
```

## Best Practices

### 1. **Use Factory Classes**
Always use factory classes to create contexts:

```java
// Good
JdbcValueContext context = JdbcValueContextFactory.fromMetaData(rs, meta, i);

// Avoid
JdbcValueContext context = JdbcValueContext.builder()...build();
```

### 2. **Check Support Before Processing**
```java
if (processor.supports(context)) {
    Object value = processor.getValue(context);
} else {
    // Handle unsupported context
}
```

### 3. **Use Generic API for New Code**
```java
// Preferred
Object value = processor.getValue(context);

// Legacy (JDBC only)
Object value = processor.getJdbcValue(jdbcContext);
```

### 4. **Add Metadata for Complex Scenarios**
```java
context.addMetadata("encoding", "UTF-8");
context.addMetadata("timezone", "UTC");
```

## Conclusion

The Value Context architecture provides a flexible, extensible foundation for supporting multiple data source types while maintaining backward compatibility with existing JDBC code. This design enables the Data-Agent platform to evolve from a JDBC-only solution to a universal data access layer.
