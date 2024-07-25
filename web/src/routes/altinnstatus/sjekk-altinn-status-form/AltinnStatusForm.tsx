import { ReactElement } from "react";
import { Alert } from "@navikt/ds-react";
import { AltinnStatus } from "../../../types/altinnStatusSchema.ts";

interface AltinnStatusFormProps {
    status: AltinnStatus;

}

const AltinnStatusForm = ({status}: AltinnStatusFormProps ): ReactElement => {
    return (
        <Alert className="max-w-prose" variant="success">
            <ul>
                <li><b>Correspondence ID:</b> {status.correspondenceId}</li>
                <li><b>Created Date:</b> {status.createdDate}</li>
                <li><b>Orgnummer:</b> {status.orgnummer}</li>
                <li><b>Sender's Reference:</b> {status.sendersReference}</li>
                <li>
                    <b>Status Changes:</b>
                    <ul>
                        {status.statusChanges.map((change, index) => (
                            <li key={index}>
                                <p>Date: {change.date}</p>
                                <p>Type: {change.type}</p>
                            </li>
                        ))}
                    </ul>
                </li>
            </ul>
        </Alert>
    );
};

export default AltinnStatusForm;