import { ReactElement } from "react";

import { Alert } from "@navikt/ds-react";

import NarmesteLedereListItem from "./NarmesteLedereListItem.tsx";
import { Narmesteleder } from "../../../types/narmesteleder.ts";

interface NarmesteLedereListProps {
  narmesteleder: Narmesteleder;
}

const NarmestelederItem = ({
  narmesteleder,
}: NarmesteLedereListProps): ReactElement => {
  return (
    <Alert className="items-start" variant="success">
      <ul>
        {Object.entries(narmesteleder).map(([key, value]) => (
          <NarmesteLedereListItem
            key={narmesteleder.fnr + key}
            narmesteledereKey={key}
            value={value}
          />
        ))}
      </ul>
    </Alert>
  );
};

export default NarmestelederItem;
