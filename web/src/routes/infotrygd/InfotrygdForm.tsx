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
    const [tkNummer, setTkNummer] = useState('')
    const [hoveddiagnoseKode, setHoveddiagnosekode] = useState('')
    const [hoveddiagnoseKodeSystem, setHoveddiagnosekodeSystem] = useState('')
    const [bidiagnoseKode, setBidiagnoseKode] = useState('')
    const [bidiagnoseKodeSystem, setBidiagnosekodeSystem] = useState('')
    const [identBehanlder, setIdentBehandler] = useState('')

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
                    setTkNummer(event.currentTarget.value)
                }}
                className="my-6 w-96"
            />
            <TextField
                label="hoveddiagnoseKode"
                size="medium"
                required
                onChange={(event) => {
                    setHoveddiagnosekode(event.currentTarget.value)
                }}
                className="my-6 w-96"
            />
            <TextField
                label="hoveddiagnoseKodeSystem"
                size="medium"
                required
                onChange={(event) => {
                    setHoveddiagnosekodeSystem(event.currentTarget.value)
                }}
                className="my-6 w-96"
            />
            <TextField
                label="bidiagnosekode"
                size="medium"
                required
                onChange={(event) => {
                    setBidiagnoseKode(event.currentTarget.value)
                }}
                className="my-6 w-96"
            />
            <TextField
                label="bidiagnosekode system"
                size="medium"
                required
                onChange={(event) => {
                    setBidiagnosekodeSystem(event.currentTarget.value)
                }}
                className="my-6 w-96"
            />
            <TextField
                label="Ident Behandler"
                size="medium"
                required
                onChange={(event) => {
                    setIdentBehandler(event.currentTarget.value)
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
