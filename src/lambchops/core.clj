(ns lambchops.core
  (:require [amazonica.aws.lambda :refer (invoke)]
            [clojure.string :refer (split)]
            [cognitect.transit :as transit]
            [clojure.java.io :as io])
  (:import [com.amazonaws.services.lambda.runtime RequestStreamHandler Context]
           [java.io ByteArrayOutputStream]))

(defn- parse-lambda-sym
  "Parses the symbol argument passed to the deflambda macro
  into [class-name fn-name]."
  [sym]
  (-> sym str (split #"\.")))

(defn make-lambda
  [class-name fn-name handler-fn]
  (defn- -handleRequest [this in out ctx]
    (let [reader (transit/reader in :json)
          writer (transit/writer out :json)]
      (->> (transit/read reader)
           handler-fn
           (transit/write writer))))
  (eval `(gen-class
          :name ~class-name
          :implements [com.amazonaws.services.lambda.runtime.RequestStreamHandler]))
  (eval `(defn ~(symbol fn-name)
           [~'x]
           (with-open [~'os (ByteArrayOutputStream. 4096)]
             (let [~'writer (transit/writer ~'os :json)]
               (transit/write ~'writer ~'x)
               (invoke :function-name ~fn-name
                       :payload (.toString ~'os)))))))

(make-lambda "HelloWorld" "hello-world" #(str "Hello, " (:name %) "!"))
