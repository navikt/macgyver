import { ReactElement } from "react";

import { Alert } from "@navikt/ds-react";

import { Person } from "../../../types/personSchema.ts";

import PersonItem from "./PersonItem";

interface PersonWithIdentProps {
  person: Person;
}

const PersonWithIdent = ({ person }: PersonWithIdentProps): ReactElement => {
  return (
    <Alert className="max-w-prose" variant="success">
      <ul className="[&>li:nth-child(2)]:mb-6">
        <PersonItem personIdentKey="fnr" value={person.fnr} />
        <PersonItem personIdentKey="navn" value={person.navn} />
        {person.identer.map((ident) =>
          Object.entries(ident).map(([key, value]) => (
            <PersonItem
              key={ident.ident + key}
              personIdentKey={key}
              value={value}
            />
          )),
        )}
      </ul>
    </Alert>
  );
};

export default PersonWithIdent;
