import "./global.css";

import React from "react";
import ReactDOM from "react-dom/client";
import { RouterProvider, createBrowserRouter } from "react-router-dom";

import Providers from "./providers.tsx";
import ErrorPage from "./error-page.tsx";
import Root from "./routes/root.tsx";
import Landing from "./routes/person/landing.tsx";
import PersonOppslag from "./routes/person/oppslag.tsx";
import OppgaveOppslag from "./routes/oppgaver/oppslag.tsx";
import EndreIdent from "./routes/ident/endre.tsx";
import SlettSykmelding from "./routes/sykmelding/slett.tsx";
import NyNarmesteleder from "./routes/narmesteleder/ny.tsx";
import OppslagNarmesteleder from "./routes/narmesteleder/oppslag.tsx";
import SlettLegeerklaering from "./routes/legeerklaering/slett.tsx";
import NotFound from "./not-found.tsx";
import JournalposterOppslag from "./routes/journalpost/oppslag.tsx";
import SykmeldingsOpplysningerOppslag from "./routes/sykmeldingsopplysninger/oppslag.tsx";
import AltinnStatusOppslag from "./routes/altinnstatus/oppslag.tsx";

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
      { path: "/sykmeldingsopplysninger/oppslag", element: <SykmeldingsOpplysningerOppslag />},
      { path: "/altinnstatus/oppslag", element: <AltinnStatusOppslag />},
      { path: "/oppgave/oppslag", element: <OppgaveOppslag /> },
      { path: "/ident/endre", element: <EndreIdent /> },
      { path: "/sykmelding/slett", element: <SlettSykmelding /> },
      { path: "/narmesteleder/ny", element: <NyNarmesteleder /> },
      { path: "/narmesteleder/oppslag", element: <OppslagNarmesteleder /> },
      { path: "/legeerklaering/slett", element: <SlettLegeerklaering /> },
      { path: "/journalpost/oppslag", element: <JournalposterOppslag /> },
      { path: "*", element: <NotFound /> },
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
