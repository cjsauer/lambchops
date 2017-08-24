(ns lambchops.hello-world
  (:require [lambchops.core :refer (deflambda)]))

(deflambda hello-world :json
  [{:strs [name]} ctx]
  (prn ctx)
  {:greeting (format "Hello, %s!" name)})
