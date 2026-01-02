# Prime Mover Janus Module

## Overview

The Janus module provides dynamic interface composition and mixin support using Java 25's ClassFile API. It enables runtime generation of composite objects that aggregate multiple mixin implementations.

**Artifact**: `com.hellblazer.primeMover:janus`

**Package**: `com.chiralbehaviors.janus`

**Type**: Core utility module

## Purpose

Janus solves the composition problem:
- How to combine multiple behaviors into a single object
- How to avoid deep inheritance hierarchies
- How to dynamically create composite types at runtime
- How to maintain type safety with mixins

## Key Concepts

### Mixins
Reusable behavior implementations that can be mixed into other types.

```java
public interface Drawable {
    void draw();
}

public class DrawableImpl implements Drawable {
    @Override
    public void draw() {
        System.out.println("Drawing");
    }
}
```

### Facets
Dependencies injected into mixin implementations.

```java
@Facet
private Logger logger;
```

### Composite
A type that combines multiple mixin implementations.

```java
// Composite interface combining multiple behaviors
public interface Shape extends Drawable, Movable, Serializable {
}

// Runtime creation
var composite = Composite.instance();
Shape shape = composite.assemble(
    Shape.class,
    Thread.currentThread().getContextClassLoader(),
    new DrawableImpl(),
    new MovableImpl()
);
```

## Usage

**Note**: All examples use the full API with explicit class loader. For simpler code, you can create a helper:

```java
public class CompositeHelper {
    private static final Composite INSTANCE = Composite.instance();

    public static <T> T assemble(Class<T> composite, Object... mixins) {
        return INSTANCE.assemble(composite,
            Thread.currentThread().getContextClassLoader(),
            mixins);
    }
}
```

Then use: `var shape = CompositeHelper.assemble(Shape.class, new DrawableImpl(), new MovableImpl());`

### Basic Mixin Definition

```java
public interface Drawable {
    void draw();
}

public class DrawableImpl implements Drawable {
    private Canvas canvas;  // Injected

    public DrawableImpl(Canvas canvas) {
        this.canvas = canvas;
    }

    @Override
    public void draw() {
        canvas.render();
    }
}
```

### Mixin with Facets

```java
public class SmartDrawable implements Drawable {
    @Facet
    private Logger logger;

    @Facet
    private Cache cache;

    @Override
    public void draw() {
        logger.info("Drawing");
        // ... use cache
    }
}
```

### Mixin with This Reference

```java
public class LoggingBehavior implements Loggable {
    @This
    private MyInterface myself;  // Reference to composite object

    @Override
    public void logState() {
        System.out.println("State: " + myself.getState());
    }
}
```

### Creating Composites

```java
// Define composite interface
public interface SmartShape extends Drawable, Movable, Loggable {
}

// Create instance at runtime
var composite = Composite.instance();
SmartShape shape = composite.assemble(
    SmartShape.class,
    Thread.currentThread().getContextClassLoader(),
    new DrawableImpl(canvas),
    new MovableImpl(),
    new LoggingBehavior()
);

// Use as normal interface
shape.draw();
shape.move(10, 20);
shape.logState();
```

## Architecture

### Composition Pipeline

```
Interface Definition (SmartShape)
    |
    v
Mixin Implementations (DrawableImpl, MovableImpl, etc.)
    |
    v
Composite.assemble()
    |
    v
Bytecode Generation (ClassFile API)
    |
    v
Dynamic Class Creation
    |
    v
Instance Creation
```

### Generated Code

When you call `Composite.assemble()`, the framework generates:

1. **Composite Class** - Implements target interface
2. **Delegation Methods** - Each interface method routes to appropriate mixin
3. **Facet Injection** - Injects dependencies into mixins
4. **This Binding** - Binds `@This` references

**Conceptual Generated Code**:
```java
public class SmartShape_Composite implements SmartShape {
    private DrawableImpl drawable;
    private MovableImpl movable;

    public SmartShape_Composite(DrawableImpl d, MovableImpl m) {
        this.drawable = d;
        this.movable = m;
        // Inject facets
        drawable.setLogger(logger);
        movable.setCache(cache);
    }

    @Override
    public void draw() {
        return drawable.draw();
    }

    @Override
    public void move(int x, int y) {
        return movable.move(x, y);
    }
}
```

## Advanced Features

### Facet Annotation

Mark mixin fields to be injected:

```java
public class DataMixin {
    @Facet
    private Database db;

    @Facet
    private Logger logger;

    public void loadData() {
        logger.info("Loading from " + db.getName());
        // ... use db
    }
}
```

### This Annotation

Access the composite object from within a mixin:

```java
public class IdentityMixin implements Identified {
    @This
    private Identified myself;

    @Override
    public String getId() {
        return myself.hashCode() + "-" + System.nanoTime();
    }
}
```

### Method Conflicts

When multiple mixins implement the same interface, only one is used.

```java
public interface Renderable {
    void render();
}

class Renderer1 implements Renderable { ... }
class Renderer2 implements Renderable { ... }

// Last mixin wins
var composite = Composite.instance();
composite.assemble(
    MyInterface.class,
    Thread.currentThread().getContextClassLoader(),
    new Renderer1(),
    new Renderer2()  // render() uses Renderer2
);
```

## Benefits

### 1. Composition Over Inheritance
- Avoid deep inheritance hierarchies
- Combine behaviors easily
- Mix and match at runtime

### 2. Type Safety
- Full compile-time type checking
- IDE support for method completion
- No casting needed

### 3. Flexibility
- Create new composites at runtime
- Change mixin combinations dynamically
- Zero code generation in source

### 4. Performance
- Efficient delegation (jitted method calls)
- No reflection overhead
- Minimal memory overhead

## Performance

- **Composite Creation**: O(n) where n = number of mixins (one-time cost)
- **Method Invocation**: O(1) - direct method call (after jit)
- **Memory**: One object instance + mixin instances
- **Typical Overhead**: <5% vs direct implementation

## Use Cases

### 1. Plugin Architecture
```java
public interface Plugin extends Named, Versioned, Executable {
}

var composite = Composite.instance();
Plugin plugin = composite.assemble(
    Plugin.class,
    Thread.currentThread().getContextClassLoader(),
    new NameMixin("MyPlugin"),
    new VersionMixin("1.0.0"),
    new ExecutableMixin()
);
```

### 2. Simulated Entities with Behaviors
```java
@Entity
public interface SimulatedAgent extends Movable, Talkative, Intelligent {
}

var composite = Composite.instance();
SimulatedAgent agent = composite.assemble(
    SimulatedAgent.class,
    Thread.currentThread().getContextClassLoader(),
    new MovementBehavior(),
    new CommunicationBehavior(),
    new DecisionMaking()
);
```

### 3. Protocol Implementations
```java
public interface HTTPServer extends Listenable, Routable, Loggable {
}

var composite = Composite.instance();
HTTPServer server = composite.assemble(
    HTTPServer.class,
    Thread.currentThread().getContextClassLoader(),
    new ListenerMixin(8080),
    new RouterMixin(),
    new LoggingMixin()
);
```

### 4. Data Access Objects
```java
public interface UserRepository extends Readable, Writable, Indexable {
}

var composite = Composite.instance();
UserRepository repo = composite.assemble(
    UserRepository.class,
    Thread.currentThread().getContextClassLoader(),
    new ReadImpl(database),
    new WriteImpl(database),
    new IndexImpl(database)
);
```

## Limitations

1. **Interface-only**: Composite must be an interface
2. **Method conflicts**: Multiple implementations of same method - last wins
3. **Constructor**: Generated composite has no public constructor (use `assemble()`)
4. **Debugging**: Generated classes are synthetic (but still debuggable)

## Integration with Prime Mover

Janus can be used to build complex simulation entities:

```java
@Entity
public interface ComplexAgent extends
    Movable,
    Communicative,
    Adaptive,
    Loggable {
}

// Create at runtime
var composite = Composite.instance();
ComplexAgent agent = composite.assemble(
    ComplexAgent.class,
    Thread.currentThread().getContextClassLoader(),
    new MovementBehavior(),
    new CommunicationBehavior(),
    new LearningBehavior(),
    new LoggingBehavior()
);

// Use in simulation
agent.move();
agent.communicate();
agent.adapt();
agent.log();
```

## Implementation Details

### ClassFile API Usage

Janus uses Java 25's ClassFile API to:
1. Analyze target interface
2. Analyze mixin implementations
3. Generate composite class bytecode
4. Handle method delegation
5. Manage facet injection

**Key Components**:
- `ClassFile` - Bytecode reading/writing
- `ClassDesc` - Type descriptors
- `MethodTypeDesc` - Method signatures
- Dynamic proxy generation

## Testing

Test your composites:

```java
@Test
void testComposite() {
    var composite = Composite.instance();
    MyInterface obj = composite.assemble(
        MyInterface.class,
        Thread.currentThread().getContextClassLoader(),
        new Impl1(),
        new Impl2()
    );

    assertEquals("expected", obj.method1());
    assertEquals(42, obj.method2());
}
```

## See Also

- **api module**: Annotations and framework contracts
- **transform module**: Bytecode transformation using ClassFile API
- **space-ghost module**: Example application using Janus
- **framework module**: Runtime for simulation entities
