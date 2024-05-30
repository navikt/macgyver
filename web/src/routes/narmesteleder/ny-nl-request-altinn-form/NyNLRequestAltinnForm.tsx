import { ReactElement, useState } from "react";
import { Button, TextField } from "@navikt/ds-react";

import ConfirmModal from "../../../components/confirm-modal/ConfirmModal.tsx";

interface NyNlRequestAltinnFormProps {
  onChange: (sykmeldingId: string, fnr: string, orgnummer: string) => void;
}

const NyNlRequestAltinnForm = ({
  onChange,
}: NyNlRequestAltinnFormProps): ReactElement => {
  const [sykmeldingId, setSykmeldingId] = useState<string>("");
  const [fnr, setFnr] = useState("");
  const [orgnummer, setOrgnummer] = useState("");

  const [conformationModalOpen, setConformationModalOpen] = useState(false);

  return (
    <div>
      <TextField
        name="sykmeldingId"
        label="sykmeldingId"
        size="medium"
        onChange={(event) => {
          setSykmeldingId(event.currentTarget.value);
        }}
        className="my-6 w-96"
      />
      <TextField
        label="fnr"
        size="medium"
        onChange={(event) => {
          setFnr(event.currentTarget.value);
        }}
        className="my-4 w-96"
      />
      <TextField
        label="orgnummer"
        size="medium"
        onChange={(event) => {
          setOrgnummer(event.currentTarget.value);
        }}
        className="my-4 w-96"
      />
      <Button
        variant="primary"
        size="medium"
        className="my-4"
        onClick={() => {
          setConformationModalOpen(true);
        }}
      >
        send
      </Button>
      <ConfirmModal
        message="Er du sikker på at du vil sending ny NL-request til altinn?"
        onCancel={() => {
          setConformationModalOpen(false);
        }}
        onOK={() => {
          onChange(sykmeldingId, fnr, orgnummer);
          setConformationModalOpen(false);
        }}
        open={conformationModalOpen}
      ></ConfirmModal>
    </div>
  );
};

export default NyNlRequestAltinnForm;
