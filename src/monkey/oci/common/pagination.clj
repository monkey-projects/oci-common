(ns monkey.oci.common.pagination
  "Provides functionality for handling paged results. See the OCI [docs on 
   pagination](https://docs.oracle.com/en-us/iaas/Content/API/Concepts/usingapi.htm#nine#ariaid-title13)
   for the details on how this works."
  (:require [schema.core :as s]))

(defn paginate
  "Repeatedly invokes `req-fn`, assuming it returns a http response that
   contains a `:body` and optionally headers that indicate whether there
   is a next page or not.  Returns a lazy seq of the response items."
  [req-fn & [item-fn]]
  (let [body-fn (cond->> :body
                  item-fn (comp item-fn))]
    (->> (iteration
          req-fn
          {:somef (comp not-empty :body)
           :vf body-fn
           :kf (comp :opc-next-page :headers)})
         (seq)
         (mapcat identity))))

(defn paged-request
  "Wraps the given client request in a paged request, suitable to be passed on to
   `paginate`.  The input `f` is assumed to be an n-arity function that accepts
   request parameters and returns a `deref`-able response.  Typically this will be 
   some function that is exposed by the OCI libraries, and that accepts the Martian 
   context and an extra arguments map.  The arity of the function is at least one,
   or the size of `args`.
   If extra arguments are given, they are passed to `f`.  If a page is passed into
   the request fn, it is `assoc`-ed into the last argument.
   Note that this does not handle error responses, that's up to `f`."
  [f & args]
  (let [opts (last args)
        t (apply partial f (butlast args))]
    (fn [page]
      (deref (t (cond-> opts
                  page (assoc :page page)))))))

(defn paged-route
  "Adds pagination query arguments to the route schema"
  [r]
  (update r :query-schema merge {(s/optional-key :page) s/Str
                                 (s/optional-key :limit) s/Int}))

(comment
  ;; An example of how a paginated response is handled
  (paginate (paged-request list-applications ctx {})))
