# rf-query

**ALPHA STATUS**

A hook-free API to use `react-query` with `re-frame`.

## Installation

```clojure
{:deps {io.github.rads/rf-query {:git/sha "<SHA>"}}}
```

## Usage

1. Set up `react-query` and pass in the `useQuery` hook to `rq/set-config!`:

```clojure
(ns rf-query.example.queries
  (:require [rf-query.core :as rq]
            ["@tanstack/react-query" :refer [useQuery QueryClient
                                             QueryClientProvider]]))

(def query-client (QueryClient.))

(defn provider [& children]
  (into [:> QueryClientProvider {:client query-client}]
        children))

(rq/set-config! {:use-query-fn useQuery})
```

2. Define your query as a map:

```clojure
(def counter
  {:query-key ["counter"]
   :query-fn (fn [] (js/Promise.resolve (rand-int 100)))})
```

3. Use `rq/subscribe` instead of `rf/subscribe` (regular subs still work):

```clojure
(ns rf-query.example
  (:require [cljs.pprint :as pprint]
            [reagent.dom :as rdom]
            [rf-query.core :as rq]
            [rf-query.example.queries :as queries]))

(def container (js/document.getElementById "app"))

(defn hello [_]
  (let [counter @(rq/subscribe queries/counter)]
    (case (:status counter)
      :loading "Loading"
      :error (str "Error: " (.-message (:error counter)))
      :success (str "Count: " (:data counter)))
    [:pre [:code (with-out-str (pprint/pprint counter))]]))

(defn app []
  [queries/provider
   [:f> hello]])

(defn -main [& _]
  (rdom/render [app] container))
```

## Caveats

- The `rq/subscribe` function uses hooks internally which means it can only be called within a function component
  - In practice this just means you need to render your components like `[:f> foo]` instead of `[foo]`

## Example

See the [`rf-query.example` namespace](https://github.com/rads/rf-query/tree/main/src/rf_query/example).

```shell
clojure -M:shadow-cljs watch app
open http://localhost:8888
```

## Roadmap

- Support `useMutation`
- Support additional config options for `useQuery`
