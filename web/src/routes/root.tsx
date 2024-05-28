import { ReactElement } from "react";
import { Outlet } from "react-router-dom";
import { Page } from "@navikt/ds-react";

import Header from "../components/layout/Header.tsx";
import Sidebar from "../components/layout/Sidebar.tsx";

function Root(): ReactElement {
  return (
    <Page contentBlockPadding="none">
      <Header />
      <div className="flex">
        <Sidebar />
        <Page.Block gutters width="2xl" as="main">
          <Outlet />
        </Page.Block>
      </div>
    </Page>
  );
}

export default Root;
