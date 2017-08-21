(ns lambchops.hello-world
  (:require [lambchops.core :refer (deflambda)]))

(deflambda hello-world
  [data]
  (str "Hello, " (get data "name") "!"))
