"use client"

import axios from 'axios'
import {useEffect, useState} from "react"
import Select from "react-select"
import PromptCard from './PromptCard'

export type Prompt = {
    title: String,
    text: String,
    rating: number,
}

const sortOptions = [
    {label: "A-Z", value: "title"},
    {label: "TR", value: "rating"},
]

const sortOrderOptions = [
    {label: "asc", value: 1},
    {label: "desc", value: -1},
]

const PromptControls = () => {
    const [promptControls, setPromptControls] = useState({sort_by: "title", sort_order: 1})
    const [prompts, setPrompts] = useState<[Prompt] | null>(null)

    useEffect(() => {
        async function query() {
            const res = await axios.get("/api/prompts", {params: promptControls})
            setPrompts(res.data as [Prompt])

        }
        query()
    }, [promptControls])

    return (
        <div className='max-w-[500px]'>
            <input className='border-2 border-white rounded-xl bg-zinc-900 h-14 min-w-[500px] my-3 px-3' placeholder='Search Prompts'/>

            <div className='flex gap-5'>

                <div className='flex flex-col gap-1'>
                    <p className='text-sm font-bold ml-2 text-gray-400'>Sort By</p>
                    <Select
                        classNames={{
                            control: () => "bg-zinc-900 border-2 border-amber-400 rounded-xl p-2",
                            menu: () => "bg-zinc-900 border-2 border-amber-400 rounded-xl p-3",
                            option: (state) => state.isFocused ? 'bg-zinc-800' : 'bg-zinc-900'
                        }}
                        unstyled
                        isSearchable={false}
                        defaultValue={sortOptions[0]}
                        options={sortOptions}
                        onChange={(newValue) => {
                            if (newValue) {
                                setPromptControls((prev) => ({...prev, sort_by: newValue.value}))
                            }
                        }}
                    />
                </div>

                <div className='flex flex-col gap-1'>
                    <p className='text-sm font-bold ml-1 text-gray-400'>Sort Order</p>
                    <Select
                        classNames={{
                            control: () => "bg-zinc-900 border-2 border-amber-400 rounded-xl p-2",
                            menu: () => "bg-zinc-900 border-2 border-amber-400 rounded-xl p-3",
                            option: (state) => state.isFocused ? 'bg-zinc-800' : 'bg-zinc-900'
                        }}
                        unstyled
                        isSearchable={false}
                        defaultValue={sortOrderOptions[0]}
                        options={sortOrderOptions}
                        onChange={(newValue) => {
                            if (newValue) {
                                setPromptControls((prev) => ({...prev, sort_order: newValue.value}))
                            }
                        }}
                    />
                </div>
            </div>

            <div className='flex flex-col gap-5 mt-12 overflow-scroll'>

                {prompts?.map((prompt) => <PromptCard prompt={prompt}/>)}

            </div>
        </div>
    )

}

export default PromptControls
