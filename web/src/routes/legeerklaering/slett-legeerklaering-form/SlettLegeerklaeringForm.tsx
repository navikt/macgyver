import { useState } from "react";
import { Button, TextField } from "@navikt/ds-react";

import ConfirmModal from "../../../components/confirm-modal/ConfirmModal.tsx";

interface SlettLegeerklaeringFormProps {
  onSubmit: (legeerklaeringId: string) => void;
}

const SlettLegeerklaeringForm = ({
  onSubmit,
}: SlettLegeerklaeringFormProps): JSX.Element => {
  const [legeerklaeringId, setlegeerklaeringId] = useState<string>("");
  const [conformationModalOpen, setConformationModalOpen] = useState(false);

  return (
    <div>
      <TextField
        name="legeerklaeringId"
        label="legeerklaeringId"
        size="medium"
        onChange={(event) => {
          setlegeerklaeringId(event.currentTarget.value);
        }}
        className="my-6 w-96"
      />
      <Button
        variant="primary"
        size="medium"
        className="my-4"
        onClick={() => {
          setConformationModalOpen(true);
        }}
      >
        Slett
      </Button>
      <ConfirmModal
        message={`Er du sikker på at du vil slette legeerklaeringId med id: ${legeerklaeringId}?`}
        onCancel={() => {
          setConformationModalOpen(false);
        }}
        onOK={() => {
          onSubmit(legeerklaeringId);
          setConformationModalOpen(false);
        }}
        open={conformationModalOpen}
      ></ConfirmModal>
    </div>
  );
};

export default SlettLegeerklaeringForm;
