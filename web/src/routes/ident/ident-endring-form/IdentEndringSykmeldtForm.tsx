import { ReactElement, useState } from "react";
import { Button, TextField } from "@navikt/ds-react";

import ConfirmationModal from "../../../components/confirm-modal/ConfirmModal";

interface IdentEndringFormProps {
  onChange: (fnr: string, nyttFnr: string) => void;
}

const IdentEndringSykmeldtForm = ({
  onChange,
}: IdentEndringFormProps): ReactElement => {
  const [fnr, setFnr] = useState("");
  const [nyttFnr, setNyttFnr] = useState("");

  const [conformationModalOpen, setConformationModalOpen] = useState(false);

  return (
    <div>
      <TextField
        label="fnr"
        size="medium"
        required
        onChange={(event) => {
          setFnr(event.currentTarget.value);
        }}
        className="my-6 w-96"
      />
      <TextField
        label="nyttFnr"
        size="medium"
        required
        onChange={(event) => {
          setNyttFnr(event.currentTarget.value);
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
        Endre
      </Button>
      <ConfirmationModal
        message="Er du sikker på at du vil endret fnr for sykmeldt?"
        onCancel={() => {
          setConformationModalOpen(false);
        }}
        onOK={() => {
          onChange(fnr, nyttFnr);
          setConformationModalOpen(false);
        }}
        open={conformationModalOpen}
      ></ConfirmationModal>
    </div>
  );
};

export default IdentEndringSykmeldtForm;
