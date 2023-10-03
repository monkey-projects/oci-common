(ns monkey.oci.common.martian
  (:require [camel-snake-kebab.core :as csk]
            [martian
             [core :as martian]
             [encoders :as me]
             [httpkit :as martian-http]
             [interceptors :as mi]]
            [monkey.oci.sign.martian :as sm]))

(defn make-context
  "Creates Martian context for the given configuration.  This context
   should be passed to subsequent requests.  The `host-fn` is a function
   that generates the host using the configuration.  Usually it formats
   the host using the region from the `conf`."
  [conf host-fn routes]
  (martian/bootstrap
   (host-fn conf)
   routes
   {:interceptors (concat martian/default-interceptors
                          [mi/default-encode-body
                           (mi/coerce-response (me/default-encoders csk/->kebab-case-keyword))
                           (sm/signer conf)
                           martian-http/perform-request])}))
