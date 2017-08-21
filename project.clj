(defproject lambchops "0.1.3"
  :description "An experiment in distributed function invocation."
  :url "https://github.com/cjsauer/lambchops"
  :license {:name "MIT"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [amazonica "0.3.111" :exclusions [com.amazonaws/aws-java-sdk
                                                   com.amazonaws/amazon-kinesis-client]]
                 [com.amazonaws/aws-java-sdk-core "1.11.179"]
                 [com.amazonaws/aws-lambda-java-core "1.1.0"]
                 [com.amazonaws/aws-java-sdk-lambda "1.11.179"]
                 [com.cognitect/transit-clj "0.8.300"]]
  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag"]
                  ["deploy" "clojars"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]]
  :aot :all)
