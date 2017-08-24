(ns lambchops.core
  (:require [amazonica.aws.lambda :refer (invoke)]
            [clojure.string :refer (split capitalize) :as s]
            [cognitect.transit :as transit]
            [clojure.java.io :as io])
  (:import [com.amazonaws.services.lambda.runtime RequestStreamHandler Context]
           [java.io ByteArrayOutputStream]))

(defn qualify
  [class-name]
  (let [ns (s/replace (str *ns*) #"-" "_")]
    (str ns "." class-name)))

(defn make-lambda
  [class-name fn-name handler-fn]
  (eval `(defn ~'-handleRequest [this# in# out# ctx#]
           (let [reader# (transit/reader in# :json)
                 writer# (transit/writer out# :json)]
             (->> (transit/read reader#)
                  (#(~handler-fn % ctx#))
                  (transit/write writer#)))))
  (eval `(gen-class
          :name ~(-> class-name qualify symbol)
          :implements [com.amazonaws.services.lambda.runtime.RequestStreamHandler]))
  (eval `(defn ~(symbol fn-name)
           [~'data]
           (with-open [os# (ByteArrayOutputStream. 4096)]
             (let [writer# (transit/writer os# :json)]
               (transit/write writer# ~'data)
               (-> (invoke :function-name ~fn-name
                           :payload (.toString os#))
                   (#(if-let [err# (:function-error %)]
                       (throw (ex-info (str "Lambda error: " err#) %))
                       %))
                   :payload
                   .array
                   io/input-stream
                   (transit/reader :json)
                   transit/read))))))

(defn camel-case
  [fname]
  (let [pieces (split (str fname) #"-")
        remove-chars #(s/replace % #"[^a-zA-Z]" "")
        work (comp capitalize remove-chars)]
    (apply str (mapcat work pieces))))

(defmacro deflambda
  [fname args & body]
  (let [class-name (camel-case fname)]
    `(make-lambda ~class-name ~(str fname) (fn ~args ~@body))))
