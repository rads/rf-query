# rf-query

**ALPHA STATUS**

A hook-free API to use `react-query` with `re-frame`.

## Installation

```clojure
{:deps {io.github.rads/rf-query {:git/tag "v0.0.2" :git/sha "c5f191e"}}}
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

3. Use the `rq/with-queries` function to wrap your component. Access data with the `[::rq/query-state query]` subscription:

```clojure
(ns rf-query.example.main
  (:require [cljs.pprint :as pprint]
            [re-frame.core :as rf]
            [reagent.dom :as rdom]
            [rf-query.core :as rq]
            [rf-query.example.queries :as queries]))

(def hello
  (rq/with-queries
    [queries/counter]
    (fn [_]
      (let [query @(rf/subscribe [::rq/query-state queries/counter])
            {:keys [status data error]} query]
        [:div
         [:div (case status
                 :loading "Loading"
                 :error (str "Error: " (.-message error))
                 :success (str "Count: " data))]
         [:pre [:code (with-out-str (pprint/pprint query))]]]))))

(defn app []
  [queries/provider
   [hello]])

(def container (js/document.getElementById "app"))

(defn -main [& _]
  (rdom/render [app] container))
```

## Example

See the [`rf-query.example` namespace](https://github.com/rads/rf-query/tree/main/src/rf_query/example).

```shell
clojure -M:shadow-cljs watch app
open http://localhost:8888
```

## Roadmap

- Support `useMutation`
- Support additional config options for `useQuery`
