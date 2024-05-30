import { ReactElement } from "react";

import { Alert } from "@navikt/ds-react";

import NarmesteldereListItem from "./NarmesteldereListItem";
import { Narmesteleder } from "../../../types/narmesteleder.ts";

interface NarmesteldereListProps {
  narmesteleder: Narmesteleder;
}

const NarmestelederItem = ({
  narmesteleder,
}: NarmesteldereListProps): ReactElement => {
  return (
    <Alert className="items-start" variant="success">
      <ul>
        {Object.entries(narmesteleder).map(([key, value]) => (
          <NarmesteldereListItem
            key={narmesteleder.fnr + key}
            narmesteldereKey={key}
            value={value}
          />
        ))}
      </ul>
    </Alert>
  );
};

export default NarmestelederItem;
