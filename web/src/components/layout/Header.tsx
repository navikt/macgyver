import { InternalHeader } from "@navikt/ds-react";
import { ReactElement } from "react";

function Header(): ReactElement {
  return (
    <InternalHeader>
      <InternalHeader.Title href="/">Macgyver</InternalHeader.Title>
    </InternalHeader>
  );
}

export default Header;
