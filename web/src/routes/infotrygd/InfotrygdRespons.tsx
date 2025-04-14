import { ReactElement } from 'react'
import { Alert } from '@navikt/ds-react'

import { InfotrygdResponseType } from '../../types/infotrygd.ts'

interface InfotrygdRespons {
    response: InfotrygdResponseType
}

export const InfotrygdResponseView = ({ response }: InfotrygdRespons): ReactElement => {
    return (
        <Alert className="max-w-prose" variant="success">
            <ul>
                <li>
                    <b>tkNummer:</b> {response.tkNummer}
                </li>
                <li>
                    <b>identDato:</b> {response.identDato}
                </li>
                <li>
                    <b>traceId:</b> {response.traceId}
                </li>
            </ul>
        </Alert>
    )
}
