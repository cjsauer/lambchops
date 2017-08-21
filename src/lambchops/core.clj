(ns lambchops.core
  (:require [amazonica.aws.lambda :refer (invoke)]
            [clojure.string :refer (split capitalize) :as s]
            [cognitect.transit :as transit]
            [clojure.java.io :as io])
  (:import [com.amazonaws.services.lambda.runtime RequestStreamHandler Context]
           [java.io ByteArrayOutputStream]))

(defn- parse-lambda-sym
  "Parses the symbol argument passed to the deflambda macro
  into [class-name fn-name]."
  [sym]
  (-> sym str (split #"\.")))

;; (defn make-lambda
;;   [class-name fn-name handler-fn]
;;   (defn- -handleRequest [this in out ctx]
;;     (let [reader (transit/reader in :json)
;;           writer (transit/writer out :json)]
;;       (->> (transit/read reader)
;;            handler-fn
;;            (transit/write writer))))
;;   (eval `(gen-class
;;           :name ~class-name
;;           :implements [com.amazonaws.services.lambda.runtime.RequestStreamHandler]))
;;   (eval `(defn ~(symbol fn-name)
;;            [~'x]
;;            (with-open [~'os (ByteArrayOutputStream. 4096)]
;;              (let [~'writer (transit/writer ~'os :json)]
;;                (transit/write ~'writer ~'x)
;;                (invoke :function-name ~fn-name
;;                        :payload (.toString ~'os)))))))

;; (defrecord HelloWorld [ifn]
;;   RequestStreamHandler
;;   (handleRequest [this in out ctx]
;;     (let [reader (transit/reader in :json)
;;           writer (transit/writer out :json)]
;;       (->> (transit/read reader)
;;            ifn
;;            (transit/write writer))))
;;   clojure.lang.IFn
;;   (invoke [this a1]
;;     (ifn a1)))

(defn camel-case
  [fname]
  (let [pieces (split (str fname) #"-")
        remove-chars #(s/replace % #"[^a-zA-Z]" "")
        work (comp capitalize remove-chars)]
    (apply str (mapcat work pieces))))

(defmacro deflambda
  [fname args & body]
  (let [record-name (symbol (camel-case fname))
        ctr-sym (symbol (str "->" record-name))]
    `(do
       (defrecord ~record-name [ifn#]
         RequestStreamHandler
         (~'handleRequest [this# in# out# ctx#]
          (let [reader# (transit/reader in# :json)
                writer# (transit/writer out# :json)]
            (->> (transit/read reader#)
                 ifn#
                 (transit/write writer#))))
         clojure.lang.IFn
         (~'invoke [this# arg#]
          (ifn# arg#)))
       (def ~fname (~ctr-sym (fn ~args ~@body))))))

(deflambda hello-world
  [m]
  (str "Hello, " (:name m) "!"))
