import { Config } from 'tailwindcss'
import naviktTailwindPreset from '@navikt/ds-tailwind'

export default {
    presets: [naviktTailwindPreset],
    content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
    theme: {
        extend: {},
    },
    plugins: [],
} satisfies Config
