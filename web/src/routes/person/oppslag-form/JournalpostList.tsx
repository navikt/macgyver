import { ReactElement } from "react";

import { Alert } from "@navikt/ds-react";

import { Journalpost } from "../../../types/journalpost.ts";

import JournalpostListItem from "./JournalpostListItem";

interface JournalpostListerProps {
  journalpostLister: Journalpost[];
}

const JournalpostList = ({
  journalpostLister,
}: JournalpostListerProps): ReactElement => {
  return (
    <Alert className="min-[700px]:max-w-[50%]" variant="success">
      <ul className="[&>li:nth-child(2n):not(:last-child)]:mb-6">
        {journalpostLister.map((journalpostList) =>
          Object.entries(journalpostList).map(([key, value]) => (
            <JournalpostListItem
              key={journalpostList.journalpostId}
              journalpostKey={key}
              value={value}
            />
          )),
        )}
      </ul>
    </Alert>
  );
};

export default JournalpostList;
