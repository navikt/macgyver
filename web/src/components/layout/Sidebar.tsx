import { ReactElement } from "react";
import { Box } from "@navikt/ds-react";
import SidebarMenuItem from "./SidebarMenuItem.tsx";
import { FilesIcon, PersonIcon } from "@navikt/aksel-icons";
import { Link } from "react-router-dom";

function Sidebar(): ReactElement {
  return (
    <Box
      background="surface-alt-3-subtle"
      padding="4"
      className="w-full max-w-sm h-[calc(100vh-48px)]"
    >
      <SidebarMenuItem title="Oppgave" Icon={FilesIcon}>
        <li>
          <Link to="/oppgave/oppslag">Hent oppgaver</Link>
        </li>
      </SidebarMenuItem>
      <SidebarMenuItem title="Person" Icon={PersonIcon}>
        <li>
          <Link to="/person/oppslag">
            Hent navn på person og liste med identer
          </Link>
        </li>
      </SidebarMenuItem>
    </Box>
  );
}

export default Sidebar;
