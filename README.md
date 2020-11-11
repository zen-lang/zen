# zen meta store & schema

## Motivation

This library is built around the "model driven design" ideas, that information
system (programm** can be decomposed into two parts -
**declarative model part** and **imperative interpreter part** (engine)

Models can be expressed with data (interoperable, regular),
more precisely **Data DSLs**.

The nice feature of Data DSLs, thats its easyly composable, regular and introspectable.

This library is implementation of **model part**.

It intrduces **model storage** and **model project**.


## Model Project

You describe your models like code in modules (**namespaces**)
and layout in file system. Set of **namespaces** can be published
and reused as a **package**.

**model project** layout is highly influenced by clojure and java,
using similar convetions.

Model project consists of set of namespaces.
Each namespace contains one or multiple models described with data.

## Namespace

Namespaces are written in [edn format]()

Namespace is a map (in terms of clojure)
with two special symbol keys  - 'ns  and 'import

* 'ns - defines name of namespace
* 'imports - is a set of required namespaces to interpret this namespace (zen namespace is imported implicitly)

Namespaces should refer other namespaces explicitly thro import!
That's how starting from one **entry point** namespace,
your project can import only used modules and models from other packages.

Rest of symbolic keys in namespace define models and keyword keys define tags.

Just like in clojure namespace you may refer one model from another located in one namespace
by short name and refer between namespaces by full name - '<namespace>/symbol 

Example namespace:

```edn
{ns myapp.module
 imports #{http}
 
 web {
  :zen/tags #{:http/server}
  :port 8080
  :workers 8
  :api api}
 
 api {
  :zen/tags #{:http/api}
  :zen/desc "API definition"
  :routes {
    :get {:operation index}
    "meta" {:operation http/api-introspection}}}

 index {
  :zen/tags #{:http/op :http/simple-op}
  :response {
    :status 302
    :headers {"location" "/index.html"}}}

}
```



## Tags

Instead of introducing any kind of types and type hierarchies,
zen uses **tag system** to classify models.

You may think about tag system as non-hierarchical multidimetional classification.
Or as a funcion of meta store - you can get all models labeled with specific tag.


TODO: keyword or symbol for tags?


## Schema

zen includes built in schema engine,
which is slightly similar to json schema.

The key features of zen schema is that it supports

* open world evalualtion - i.e. each schema validates only known by this schema keys (properties)
* ignore, warn on fail on "unknown keys" is just a validation mode not part of schema semantic
* supports RDF inspired property schema - i.e. schema attached to key name not a key container

```
{ns myapp.schema

 Contact {
   :zen/tags #{:zen/schema}
   :keys {
     :system {:type zen/string :enum [{:value "phone"} {:value "email"}]
     :value  {:type zen/string}}}

 Contactable {
   :zen/tags #{:zen/schema}
   :keys {:contacts {:type zen/vector 
                     :every {:type map :confirms #{Contact}}}}}

 User {
   :zen/tags #{:zen/schema}
   :type zen/map
   :confirms #{Contactable}
   :keys {
     :id {:type zen/string}
     :password {:type zen/string }}}

}
```

## Store

Model project may be loaded into **store**.
You start loading from **entry point namespace**.
All imports will be resolved, validated and loaded into store.

Store functions:
* get model by name <ns>/<name>
* get namespace by name
* get all models by tag
* reload namespace


## Schema Specification

Composable, open-world schema engine.

Each schema node:

* type (required) - defines interpreter and link to type specific schema keys
* confirms - set of other schemas to evaluate
* enum
* constant


For example `zen/map` type defines:

* values:  schema - schema to apply to all values
* keys: { key: schema } - enumeration of keys and schema for each key
* require: #{:key,...} - list of requried keys in map
* schema-key: {:key :some-key } - key to resolve schema from data


List of built-in types:

* primitives
  * zen/symbol
  * zen/keyword
  * zen/string
  * zen/number
  * zen/integer
  * zen/boolean
  * zen/date
  * zen/datetime
* collections
  * zen/vector
  * zen/set
  * zen/map
* zen/union-map
* zen/union

Schema can be extended with primitives and container types.
