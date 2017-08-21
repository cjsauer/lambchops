# lambchops

A Clojure library designed to experiment with distributed function invocation
and its benefits.

## Usage

```Clojure
(ns something.awesome
  (:require [lambchops.core :refer (deflambda)]))
  
(deflambda hello-world
  [data]
  (str "Hello, " (get data "name") "!"))
```

This will do a few things behind the scenes:

- Generates a class called `HelloWorld` in the current namespace that implements
  [RequestStreamHandler](http://docs.aws.amazon.com/lambda/latest/dg/java-handler-using-predefined-interfaces.html).
  In the example above, this would be `something.awesome.HelloWorld`.
- Generates an internal function `-handleRequest` that delegates to the function
  defined using the `deflambda` macro above.
- Generates a function `hello-world` that invokes the remote Lambda function
  when called.
  
Serialization and deserialization are handled automatically everywhere you'd
expect using [Transit](https://github.com/cognitect/transit-clj). When you
invoke the `hello-world` function above, it executes the remote Lambda function
and deserializes the return value. The same goes for serialization of the
arguments passed in.

```Clojure
(hello-world {"name" "Hobbes"})
;; "Hello, Hobbes!"
```

Clojure data goes in, Clojure data comes out. This makes it very easy to chain
Lambda functions together.

### Sample CloudFormation template

The above Lambda function could be specified in a CloudFormation template like so:

```YAML
AWSTemplateFormatVersion: '2010-09-09'
Transform: 'AWS::Serverless-2016-10-31'
Resources:
  HelloWorldLambda:
    Type: 'AWS::Serverless::Function'
    Properties:
      FunctionName: hello-world
      Description: 'Returns a friendly greeting.'
      Handler: 'something.awesome.HelloWorld'
      Runtime: 'java8'
      CodeUri: 's3://my-bucket/path/to/uberjar'
      MemorySize: 512
      Timeout: 15
```

## License

Copyright © 2017 Calvin Sauer

Distributed under the MIT License
