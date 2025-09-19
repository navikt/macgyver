import { ReactElement, useState } from 'react'
import { Alert, BodyShort, Button, Loader } from '@navikt/ds-react'
import { useMutation } from '@tanstack/react-query'

import { Narmesteleder } from '../../../types/narmesteleder.ts'
import ConfirmModal from '../../../components/confirm-modal/ConfirmModal.tsx'
import { MessageSchema } from '../../../api/message.ts'
import { fetchApi } from '../../../api/api.ts'

import NarmesteledereListItem from './NarmesteledereListItem'

interface NarmesteledereListProps {
    narmesteleder: Narmesteleder
}

const NarmestelederItem = ({ narmesteleder }: NarmesteledereListProps): ReactElement => {
    const [conformationModalOpen, setConformationModalOpen] = useState(false)
    const { data, isSuccess, isPending, error, mutate } = useMutation({
        mutationFn: async (params: { id: string }) => {
            return await fetchApi(`/narmesteleder/${params.id}`, {
                headers: {
                    fnr: narmesteleder.fnr,
                    orgnummer: narmesteleder.orgnummer,
                },
                method: 'DELETE',
                schema: MessageSchema,
            })
        },
    })
    return (
        <Alert className="items-start" variant="success">
            <ul>
                {Object.entries(narmesteleder)
                    .filter(([key]) => key !== 'id')
                    .map(([key, value]) => (
                        <NarmesteledereListItem key={narmesteleder.fnr + key} narmesteLedereKey={key} value={value} />
                    ))}
            </ul>
            {isPending && !error && <Loader size="medium" />}
            {isSuccess && (
                <Alert variant="success">
                    <BodyShort spacing>Koblingen er deaktivert.</BodyShort>
                    <BodyShort size="small">MacGyver sa: {data.message}</BodyShort>
                </Alert>
            )}
            {error && <Alert variant="error">{error.message}</Alert>}
            <Button
                variant="danger"
                size="medium"
                className="my-4"
                onClick={() => {
                    setConformationModalOpen(true)
                }}
            >
                Deaktiver
            </Button>
            <ConfirmModal
                message="Er du sikker på at du vil deaktivere denne NL-kobliging"
                onCancel={() => {
                    setConformationModalOpen(false)
                }}
                onOK={() => {
                    mutate({ id: narmesteleder.narmesteLederId })
                    setConformationModalOpen(false)
                }}
                open={conformationModalOpen}
            ></ConfirmModal>
        </Alert>
    )
}

export default NarmestelederItem
