import { ReactElement, useState } from 'react'
import { Alert, Button, TextField } from '@navikt/ds-react'

import ConfirmModal from '../../../components/confirm-modal/ConfirmModal.tsx'

interface NyNlRequestAltinnFormProps {
    onChange: (sykmeldingId: string, fnr: string, orgnummer: string) => void
}

const NyNlRequestAltinnForm = ({ onChange }: NyNlRequestAltinnFormProps): ReactElement => {
    const [sykmeldingId, setSykmeldingId] = useState<string>('')
    const [fnr, setFnr] = useState('')
    const [orgnummer, setOrgnummer] = useState('')
    const [conformationModalOpen, setConformationModalOpen] = useState(false)
    const [error, setError] = useState<string>('')

    const getMod11 = (strValue: string): number => {
        let checkNbr = 2
        let mod = 0

        for (let i = strValue.length - 2; i >= 0; --i) {
            mod += parseInt(strValue.charAt(i), 10) * checkNbr
            if (++checkNbr > 7) {
                checkNbr = 2
            }
        }
        const result = 11 - (mod % 11)
        return result === 11 ? 0 : result
    }

    function isValidOrgNumber(orgnummer: string): boolean {
        return (
            orgnummer.length === 9 &&
            /^[0-9]*$/.test(orgnummer) &&
            (orgnummer.startsWith('8') || orgnummer.startsWith('9')) &&
            getMod11(orgnummer) === parseInt(orgnummer.charAt(8), 10)
        )
    }

    return (
        <div>
            <TextField
                name="sykmeldingId"
                label="sykmeldingId"
                size="medium"
                onChange={(event) => {
                    setSykmeldingId(event.currentTarget.value)
                }}
                className="my-6 w-96"
            />
            <TextField
                label="fnr"
                size="medium"
                onChange={(event) => {
                    setFnr(event.currentTarget.value)
                }}
                className="my-4 w-96"
            />
            <TextField
                label="orgnummer"
                size="medium"
                onChange={(event) => {
                    const orgnummer = event.currentTarget.value.trim()
                    setOrgnummer(orgnummer)
                }}
                className="my-4 w-96"
            />
            {error && <Alert variant="error">{error}</Alert>}
            <Button
                variant="primary"
                size="medium"
                className="my-4"
                onClick={() => {
                    if (isValidOrgNumber(orgnummer)) {
                        setError('')
                        setConformationModalOpen(true)
                    } else {
                        setError('feil orgnummer ikke valid')
                    }
                }}
            >
                send
            </Button>
            <ConfirmModal
                message="Er du sikker på at du vil sending ny NL-request til altinn?"
                onCancel={() => {
                    setConformationModalOpen(false)
                }}
                onOK={() => {
                    onChange(sykmeldingId, fnr, orgnummer)
                    setConformationModalOpen(false)
                }}
                open={conformationModalOpen}
            ></ConfirmModal>
        </div>
    )
}

export default NyNlRequestAltinnForm
