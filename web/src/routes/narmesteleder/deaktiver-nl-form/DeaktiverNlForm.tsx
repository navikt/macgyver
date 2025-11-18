import { ReactElement, useState } from 'react'
import { Alert, Button, TextField } from '@navikt/ds-react'

import ConfirmModal from '../../../components/confirm-modal/ConfirmModal.tsx'

interface DeaktiverNarmestelederProps {
    onChange: (nlId: string, fnr: string, orgnummer: string) => void
}

const DeaktiverNarmestelederForm = ({ onChange }: DeaktiverNarmestelederProps): ReactElement => {
    const [id, setId] = useState<string>('')
    const [fnr, setFnr] = useState<string>('')
    const [orgnummer, setOrgnummer] = useState<string>('')
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
                name="id"
                label="Narmesteleder id"
                size="medium"
                onChange={(event) => {
                    setId(event.currentTarget.value)
                }}
                className="my-6 w-96"
            />
            <TextField
                name="fnr"
                label="Fnr"
                size="medium"
                onChange={(event) => {
                    setFnr(event.currentTarget.value)
                }}
                className="my-6 w-96"
            />
            <TextField
                name="orgnummer"
                label="Orgnummer"
                size="medium"
                onChange={(event) => {
                    setOrgnummer(event.currentTarget.value)
                }}
                className="my-6 w-96"
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
                message="Er du sikker på at du vil Deaktivere denne nærmestelederkobling?"
                onCancel={() => {
                    setConformationModalOpen(false)
                }}
                onOK={() => {
                    onChange(id, fnr, orgnummer)
                    setConformationModalOpen(false)
                }}
                open={conformationModalOpen}
            ></ConfirmModal>
        </div>
    )
}

export default DeaktiverNarmestelederForm
