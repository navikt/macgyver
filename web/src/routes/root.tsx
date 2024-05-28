import { ReactElement } from "react";
import { Outlet } from "react-router-dom";
import { Box, Page } from "@navikt/ds-react";

import Header from "../components/layout/Header.tsx";
import Sidebar from "../components/layout/Sidebar.tsx";

function Root(): ReactElement {
  return (
    <Page
      footerPosition="belowFold"
      footer={
        <Box background="surface-neutral-moderate" padding="8" as="footer">
          <Page.Block gutters width="lg">
            Footer
          </Page.Block>
        </Box>
      }
    >
      <Header />
      <div className="flex">
        <Sidebar />
        <Page.Block gutters width="lg" as="main">
          <Outlet />
        </Page.Block>
      </div>
    </Page>
  );
}

export default Root;
