(ns lambchops.hello-world
  (:require [lambchops.core :refer (deflambda)]))

(deflambda hello-world
  [data ctx]
  (prn ctx)
  (str "Hello, " (get data "name") "!"))
