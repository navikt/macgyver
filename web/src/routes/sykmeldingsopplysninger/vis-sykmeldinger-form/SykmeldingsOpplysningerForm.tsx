import {ReactElement} from "react";
import {Alert} from "@navikt/ds-react";
import {SykmeldingsOpplysninger} from "../../../types/sykmeldingsOpplysningerSchema.ts";
import SykmeldingItem from "../../sykmelding/slett-sykmelding-form/SykmeldingItem.tsx";

interface SykmeldingsOpplysningerProps {
    person: SykmeldingsOpplysninger;
}

const SykmeldingsOpplysningerForm = ({ person }: SykmeldingsOpplysningerProps): ReactElement => {
    return (
        <Alert className="max-w-prose" variant="success">
            <ul className="[&>li:nth-child(2)]:mb-6">

                {person.sykmeldinger.map((sykmelding) => (
                    <SykmeldingItem
                        key={sykmelding.sykmeldingId}
                        fnr={person.fnr}
                        sykmeldingId={sykmelding.sykmeldingId}
                        mottattTidspunkt={sykmelding.mottattTidspunkt}
                        mottakId={sykmelding.mottakId}
                        statusEvent={sykmelding.statusEvent}
                        merknader={sykmelding.merknader}
                        behandlingsutfall={sykmelding.behandlingsUtfall}
                        tssid={sykmelding.tssId}
                        perioder={sykmelding.perioder}
                    />
                ))}

            </ul>
        </Alert>
    );
};

export default SykmeldingsOpplysningerForm;
