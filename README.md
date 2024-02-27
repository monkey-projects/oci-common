# OCI Common functionality lib

This is a Clojure library that is used by the other Monkey Projects OCI
libs for common functionality.  Usually utility functions but also some
[Martian](https://github.com/oliyh/martian) stuff too.

[![Clojars Project](https://img.shields.io/clojars/v/com.monkeyprojects/oci-common.svg)](https://clojars.org/com.monkeyprojects/oci-common)

## Usage

Normally you won't need this directly, it's referenced by libs that use it
as a dependency.  But if you really want to:

```clojure
# deps.edn
{:com.monkeyprojects/oci-common {:mvn/version "latest"}}
```
Or:
```clojure
# Leiningen project.clj
[com.monkeyprojects/oci-common "latest"]
```

## Pagination

Many listing calls on OCI allow for pagination.  By default the page size is 10.
Processing a paged response can be tedious, so I've added some functions to help
with that.  Suppose you have a function that sends a request that returns a large
list of items.  Then you can use the functions in `monkey.oci.common.pagination`
to help with that:

```clojure
(require '[monkey.oci.common.pagination :as p])

;; Let's assume you need to fetch a large list using `large-list-call`
;; then you can process the paged response in a lazy fashion like so
(def r (p/paginate (p/paged-request large-list-call ctx {})))

;; It will return a lazy seq to the items in the body.
```

Often, the items in the body are not in the root, but in a map that has an `items`
key.  An additional argument can be passed to the `paginate` function to help with
that:
```clojure
;; This call will return all values in the 'items' key in the response body
(p/paginate (p/paged-request large-list-call ctx {}) :items)
```

As long as the response contains a `opc-next-page` header, and the body items are
not empty, it will keep on returning values.

**Please not** that it's important that the route is marked as 'paginate-able' by
wrapping it using the `paged-route` function.  This will add the `page` and `limit`
query parameters to the Martian route, otherwise they will not be passed on to the
backend and you may end up with an endless seq, because it will never pass on the
`page` parameter.

# License

MIT License, see [LICENSE](LICENSE)