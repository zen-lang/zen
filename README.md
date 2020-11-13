# zen meta store & schema

[![Build Status](https://travis-ci.org/HealthSamurai/zen.svg?branch=master)](https://travis-ci.org/HealthSamurai/zen)


## Motivation

This library is built around the "model driven design" - idea, that information
system can be split into two parts -
**declarative model part** and **imperative interpreter part** (engine)

Models are expressed with data or more precisely **Data DSLs**.

The nice feature of Data DSLs, thats data is easyly 
composable, regular and introspectable.

Zen library implements - model part:  **model project**, **model store** and **schema language**


## Model Project

You put your models like code in modules (**namespaces**)
and layout in file system. Set of **namespaces** can be published
and reused as a **package**.

**model project** layout is highly influenced by clojure and java,
uses similar convetions.

Model project consists of set of namespaces and may refer other packages.

### Namespace

Each namespace contains one or multiple models described with data.

Namespaces are written in [edn format](https://github.com/edn-format/edn)

Namespace is a map (in terms of edn)
with two special symbol keys  - 'ns  and 'import

* 'ns - defines name of namespace
* 'imports - is a set of required namespaces to interpret this namespace

Namespaces should refer other namespaces explicitly thro import!
`zen` namespace is imported implicitly.

That's how starting from one **entry point** namespace,
your project can import only used modules and models from other packages.

Rest of symbolic keys in namespace define models.

Just like in clojure namespace you may refer one model from another located in one namespace
by short name (`symbol`) and refer between namespaces by full name - (`namespace.name/symbol`)

Example namespace:

```edn

{ns myapp.module ;; namespace name
 imports #{zen.http} ;; imports - TODO: think about aliases
 
 ;; model
 web {
  :zen/tags #{zen.http/server} ;; tags set
  :port 8080
  :workers 8
  :api api ;; local reference to myapp.module/api model
 }
 
 api {
  :zen/tags #{zen.http/api}
  :zen/desc "API definition"
  :routes {
    :get {:operation index}
    "meta" {:operation http/api-introspection}}}

 index {
  :zen/tags #{zen.http/op zen.http/simple-op}
  :response {
    :status 302
    :headers {"location" "/index.html"}}}}
```



## Tags

Instead of introducing any kind of types and type hierarchies,
zen uses **tag system** to classify models.

You may think about tag system as non-hierarchical multidimetional classification.
Or as a funcion of meta store - get all models labeled with specific tag.

## Store

Model project may be loaded into **store**.
You start loading from **entry point namespace**.
All imports will be resolved, validated and loaded into store.

Store functions:
* get model by name `ns/sym`
* get namespace by name
* get all models by tag
* reload namespace


## Schema

zen includes builrin schema engine,
which is sinilar json schema.

The key features of zen schema is:

* open world evalualtion - i.e. each schema validates only known keys (properties)
* ignore, warn on fail on "unknown keys" is just a validation **mode**, i.e.  not part of schema semantic
* support of RDF inspired property schema - i.e. schema attached to key  (only namespaced keys) -  not to a key container

```edn
{ns myapp

 Contact {
   :zen/tags #{zen/schema}
   :type zen/map
   :keys {
     :system {:type zen/string :enum [{:value "phone"} {:value "email"}]
     :value  {:type zen/string}}}

 Contactable {
   :zen/tags #{zen/schema}
   :type zen/map
   :keys {:contacts {:type zen/vector 
                     :every {:type map :confirms #{Contact}}}}}

 User {
   :zen/tags #{zen/schema}
   :type zen/map
   :confirms #{Contactable}
   :require #{:id}
   :keys {
     :id {:type zen/string}
     :email {:type zen/string :regex #".*@.*"}
     :password {:type zen/string }}}

 ;; example of property schema
 
 human-name {
   :zen/tags #{zen/property zen/schema}
   :type zen/map
   :keys {:family {:type zen/string} 
          :given {:type zen/vector :every {:type zen/string}}}}}

;; valid user

{:id "niquola"
 :myapp/human-name {:given ["Nikolai"] :family "Ryzhikov"}
 :password #scrypt"secret"}
```


### Schema Specification

Schema can be extended with primitives and container types.

Each schema node:

* `:type` (required) - defines interpreter and link to type specific schema keys
* `:confirms` - set of other schemas to confirm (this is not inheretance!)
* `:enum` - polymorphic enumeration of possible values (TODO: think about terminology - reference semantic?)
* `:constant` - polymorphic fixed value validation


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
* zen/case

### zen/case

`zen/case` is alternative to union type,
it is more advanced and may be applied to different maps

```edn

{:zen/tags #{'zen/schema}
 :type 'zen/vector
 :every {:type 'zen/case
         :case [{:when {:type 'zen/string}}
                {:when {:type 'zen/map}
                 :then {:type 'zen/map :require #{:name} :keys {:name {:type 'zen/string}}}}]}}

```

### zen/symbol

### zen/map

For example `zen/map` type defines following validation keys:

* `:values`  schema - schema to apply to all values
* `:keys` { key: schema } - enumeration of keys and schema for each key
* `:require` #{:key,...} - list of requried keys in map
* `:schema-key` {:key :some-key } - key to resolve schema from data on fly



### zen/vector

Apply clojure.spec regular expressions for collections!!!!

* :every  schema - apply schema to every element in collection
* :nth {integer: schema} - apply schema to nth element
* :minItems & :maxItems - min/max items in collection 
* :filter - TODO: apply filter to collection, then apply schema to 


## EDN parser

Comming with basis for LSP for zen models. 

TODO: see https://github.com/borkdude/edamame
