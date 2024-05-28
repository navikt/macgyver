import {
  useQuery,
  QueryClient,
  QueryClientProvider,
} from "@tanstack/react-query";
import { ReactElement } from "react";

const queryClient = new QueryClient();

function App(): ReactElement {
  return (
    <QueryClientProvider client={queryClient}>
      <h1>Vite + React + Ktor</h1>
      <ExampleFetch />
    </QueryClientProvider>
  );
}

function ExampleFetch(): ReactElement {
  const query = useQuery<{ navn: string }>({
    queryKey: ["person"],
    queryFn: async () => {
      const response = await fetch("/api/person", {
        headers: {
          fnr: "12345678901",
        },
      });
      return response.json();
    },
  });

  if (query.isFetching) {
    return <div>Loading...</div>;
  }

  if (query.isError) {
    return <div>Error: {query.error.message}</div>;
  }

  return <div>Hello person {query.data?.navn ?? "missing"}</div>;
}

export default App;
