import { ReactElement } from "react";
import { Link } from "react-router-dom";

import logo from "./logo.webp";
import { Heading } from "@navikt/ds-react";
import { ChatIcon } from "@navikt/aksel-icons";

function Header(): ReactElement {
  return (
    <div className="p-4 border-b border-b-border-subtle flex justify-between h-20">
      <Link to="/" className="flex items-center gap-3">
        <img src={logo} height="64" width={64} alt="MacGyver!" />
        <Heading size="large" level="1" className="text-text-default">
          MacGyver - Team Sykmelding
        </Heading>
      </Link>
      <a
        href="https://nav-it.slack.com/archives/CMA3XV997"
        target="_blank"
        rel="noreferrer"
        className="flex items-center"
      >
        <ChatIcon className="mr-2" />
        <span>#team-sykmelding</span>
      </a>
    </div>
  );
}

export default Header;
