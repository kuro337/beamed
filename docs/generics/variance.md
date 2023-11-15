### Variance

###### Convariance `out` vs. Contravariance `in`

***Covariance (out)***

- Type parameter with out, like `SerializableEntity<out BeamEntity>`, means
  `SerializableEntity` can produce instances of `BeamEntity` or its subtypes, but not consume them.

- Similar to Java's `? extends T` wildcard, which allows for read-only interaction with the collection, providing type
  safety by ensuring you can't accidentally insert an incorrect type into the collection.

- Useful when returning a generic type as an output.

- For example, a `List<out Animal>` could contain `Cat` or `Dog` objects, and when you retrieve them, you treat them
  as `Animal` without worrying about their specific subtypes.

***Contravariance (in)***

- Contravariance is the opposite.

- By using in, you make a type parameter contravariant, meaning it can be assigned with
  its own type or a supertype of its type. This means your class can consume instances of T but not produce them as
  outputs. Similar to Java's `? super T` wildcard.

- Useful when passing a generic type as an input.

- For example, a `Comparator<in String>` can be used
  wherever a `Comparator<Object>` is required because a comparator of strings can naturally compare objects (since all
  strings are also objects).

***Invariance***

- In `Kotlin`, without out or in, a type is invariant. This means there's no subtype relationship at
  all: `Container<Cat>` is not a subtype or supertype of `Container<Animal>`, and you can neither pass `Container<Cat>`
  where `Container<Animal>` is expected nor vice versa.