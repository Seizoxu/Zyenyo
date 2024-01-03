"use client"

import axios from 'axios'
import {useEffect, useRef, useState} from "react"
import Select from "react-select"
import PromptCard from './PromptCard'
import { MinusCircleIcon, PlusCircleIcon } from "@heroicons/react/24/outline";
import { useDebouncedCallback } from "use-debounce"



export type Prompt = {
    title: String,
    text: String,
    rating: number,
}

const sortOptions = [
    {label: "Title", value: "title"},
    {label: "TR", value: "rating"},
]

const sortOrderOptions = [
    {label: "asc", value: 1},
    {label: "desc", value: -1},
]

const PromptControls = () => {
    const [promptControls, setPromptControls] = useState({sort_by: "title", sort_order: 1, page: 1})
    const [prompts, setPrompts] = useState<[Prompt] | null>(null)
    const debounced = useDebouncedCallback((value) => setPromptControls((prev) => ({...prev, search_query: value})), 500)

    useEffect(() => {
        async function query() {
            const res = await axios.get("/api/prompts", {params: promptControls})
            setPrompts(res.data as [Prompt])

        }
        query()
    }, [promptControls])

    return (
        <div className='max-w-[300px] md:max-w-[500px]'>
            <input className='flex border-2 border-white rounded-xl w-full bg-zinc-900 h-14 my-3 px-3' placeholder='Search Prompts' onChange={(e) => debounced(e.target.value)}/>


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
                                setPromptControls((prev) => ({...prev, sort_by: newValue.value, page: 1}))
                            }
                        }}
                    />
                </div>

                <div className='flex flex-col gap-1'>
                    <p className='text-sm font-bold ml-1 text-gray-400'>Order</p>
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
                                setPromptControls((prev) => ({...prev, sort_order: newValue.value, page: 1}))
                            }
                        }}
                    />
                </div>

                <div className='flex flex-col gap-1'>
                    <p className='text-sm font-bold ml-1 text-gray-400'>Page</p>
                    <div className='flex flex-row justify-between items-center bg-zinc-900 border-2 border-amber-400 rounded-xl p-2 w-[100px]'>
                        <button onClick={() => setPromptControls((prev) => ({...prev, page: Math.max(prev.page - 1, 1) }))}>
                            <MinusCircleIcon className="h-6 w-6 text-amber-400" />
                        </button>
                        {promptControls.page}
                        <button onClick={() => setPromptControls((prev) => ({...prev, page: Math.min(prev.page + 1, 20) }))}>
                            <PlusCircleIcon className="h-6 w-6 text-amber-400" />
                        </button>
                    </div>
                </div>
            </div>

            <div className='flex flex-col h-[70vh] gap-5 mt-12 overflow-y-scroll'>

                {prompts?.map((prompt, idx) => <PromptCard prompt={prompt} key={idx}/>)}

            </div>
        </div>
    )

}

export default PromptControls
