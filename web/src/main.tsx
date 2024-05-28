import "./global.css";

import React from "react";
import ReactDOM from "react-dom/client";
import { RouterProvider, createBrowserRouter } from "react-router-dom";

import Providers from "./providers.tsx";
import ErrorPage from "./error-page.tsx";
import Root from "./routes/root.tsx";
import Landing from "./routes/person/landing.tsx";
import PersonOppslag from "./routes/person/oppslag.tsx";

const router = createBrowserRouter([
  {
    path: "/",
    element: <Root />,
    errorElement: <ErrorPage />,
    children: [
      {
        path: "/",
        element: <Landing />,
      },
      { path: "/person/oppslag", element: <PersonOppslag /> },
    ],
  },
]);

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <Providers>
      <RouterProvider router={router} />
    </Providers>
  </React.StrictMode>,
);
