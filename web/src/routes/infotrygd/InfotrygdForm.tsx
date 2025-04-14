import React, { ReactElement, useState } from 'react'
import { Button, TextField } from '@navikt/ds-react'

interface InfotrygdFormProps {
    onChange: (
        ident: string,
        tkNummer: string | null,
        hoveddiagnose: string | null,
        hoveddiagnoseKodeSystem: string | null,
        bidiagnose: string | null,
        bidiagnoseKodeSystem: string | null,
        identBehanlder: string | null,
    ) => void
}

const InfotrygdForm = ({ onChange }: InfotrygdFormProps): ReactElement => {
    const [ident, setIdent] = useState('')
    const [tkNummer, setTkNummer] = useState<string | null>(null)
    const [hoveddiagnoseKode, setHoveddiagnosekode] = useState<string | null>(null)
    const [hoveddiagnoseKodeSystem, setHoveddiagnosekodeSystem] = useState<string | null>(null)
    const [bidiagnoseKode, setBidiagnoseKode] = useState<string | null>(null)
    const [bidiagnoseKodeSystem, setBidiagnosekodeSystem] = useState<string | null>(null)
    const [identBehanlder, setIdentBehandler] = useState<string | null>(null)

    const handleClick: React.MouseEventHandler<HTMLButtonElement> = (event) => {
        event.preventDefault()
        onChange(
            ident,
            tkNummer,
            hoveddiagnoseKode,
            hoveddiagnoseKodeSystem,
            bidiagnoseKode,
            bidiagnoseKodeSystem,
            identBehanlder,
        )
    }
    return (
        <div>
            <TextField
                label="ident"
                size="medium"
                required
                onChange={(event) => {
                    setIdent(event.currentTarget.value)
                }}
                className="my-6 w-96"
            />
            <TextField
                label="tkNummer"
                size="medium"
                required
                onChange={(event) => {
                    const value = event.currentTarget.value
                    setTkNummer(value === '' ? null : value)
                }}
                className="my-6 w-96"
            />
            <TextField
                label="hoveddiagnoseKode"
                size="medium"
                required
                onChange={(event) => {
                    const value = event.currentTarget.value
                    setHoveddiagnosekode(value === '' ? null : value)
                }}
                className="my-6 w-96"
            />
            <TextField
                label="hoveddiagnoseKodeSystem"
                size="medium"
                required
                onChange={(event) => {
                    const value = event.currentTarget.value
                    setHoveddiagnosekodeSystem(value === '' ? null : value)
                }}
                className="my-6 w-96"
            />
            <TextField
                label="bidiagnosekode"
                size="medium"
                required
                onChange={(event) => {
                    const value = event.currentTarget.value
                    setBidiagnoseKode(value === '' ? null : value)
                }}
                className="my-6 w-96"
            />
            <TextField
                label="bidiagnosekode system"
                size="medium"
                required
                onChange={(event) => {
                    const value = event.currentTarget.value
                    setBidiagnosekodeSystem(value === '' ? null : value)
                }}
                className="my-6 w-96"
            />
            <TextField
                label="Ident Behandler"
                size="medium"
                required
                onChange={(event) => {
                    const value = event.currentTarget.value
                    setIdentBehandler(value === '' ? null : value)
                }}
                className="my-6 w-96"
            />
            <Button
                variant="primary"
                size="medium"
                className="my-4"
                onClick={handleClick}
            >
                Hent
            </Button>
        </div>
    )
}

export default InfotrygdForm
